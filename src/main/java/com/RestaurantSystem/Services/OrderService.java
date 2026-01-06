package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.ENUMs.CustomOrderPriceRule;
import com.RestaurantSystem.Entities.ENUMs.PrintCategory;
import com.RestaurantSystem.Entities.Order.DTOs.*;
import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.OrderItemDTO;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Printer.PrintSync;
import com.RestaurantSystem.Entities.Order.OrdersItems;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Product.ProductOption;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs.IFoodCreateOrderDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.*;
import com.RestaurantSystem.Services.AuxsServices.PrintSyncService;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import com.RestaurantSystem.WebSocket.SignalR;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static com.RestaurantSystem.Services.Utils.DeliveryFeeAndDistance.*;

@Service
public class OrderService {

    private final OrderRepo orderRepo;
    private final OrdersItemsRepo ordersItemsRepo;
    private final OrdersItemsCancelledRepo ordersItemsCancelledRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final ShiftRepo shiftRepo;
    private final VerificationsServices verificationsServices;
    private final PrintSyncService printSyncService;
    private final PrintSyncRepo printSyncRepo;
    private final SignalR signalR;

    public OrderService(OrderRepo orderRepo, OrdersItemsRepo ordersItemsRepo, OrdersItemsCancelledRepo ordersItemsCancelledRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo,
                        ShiftRepo shiftRepo, VerificationsServices verificationsServices, PrintSyncService printSyncService, PrintSyncRepo printSyncRepo, SignalR signalR) {
        this.orderRepo = orderRepo;
        this.ordersItemsRepo = ordersItemsRepo;
        this.ordersItemsCancelledRepo = ordersItemsCancelledRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.shiftRepo = shiftRepo;
        this.verificationsServices = verificationsServices;
        this.printSyncService = printSyncService;
        this.printSyncRepo = printSyncRepo;
        this.signalR = signalR;
    }

    // <> ---------- Methods ---------- <>

    @Transactional
    public Order createOrder(String requesterID, CreateOrderDTO orderToCreate) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(orderToCreate.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Customer customer = orderToCreate.customerID() != null ? findCustomerOnCompany(company, orderToCreate.customerID()) : null;
        Shift currentShift = verificationsServices.retrieveCurrentShift(company);

        if (orderToCreate.tableNumberOrDeliveryOrPickup().equals("delivery"))
            deliveryVerifications(company, customer, orderToCreate.deliveryDistanceKM());
        if (orderToCreate.tableNumberOrDeliveryOrPickup().equals("pickup"))
            pickUpVerifications(customer, orderToCreate.pickupName());

        if (!orderToCreate.tableNumberOrDeliveryOrPickup().equals("delivery") && !orderToCreate.tableNumberOrDeliveryOrPickup().equals("pickup"))
            isTableAvailable(company, orderToCreate.tableNumberOrDeliveryOrPickup(), null);

        Order order = new Order(requester, currentShift, (currentShift.getOrders().size() + 1), orderToCreate, customer);
        if (order.getTableNumberOrDeliveryOrPickup().equals("delivery"))
            order.setDeliveryTax(customerDeliveryFeePlusExtraFee(company, customer));
        Order orderCreated = orderRepo.save(order);

        List<OrdersItems> ordersItems = mapOrderItems(orderCreated, orderToCreate.orderItemsIDs(), company, null);

        orderCreated.setOrderItems(ordersItems);
        calculateTotalPriceTaxAndDiscount(company, order, null);
        orderRepo.save(orderCreated);
//        signalR.sendShiftOperationSigr(company);

        return orderRepo.findById(orderCreated.getId()).orElseThrow(() -> new RuntimeException("Order not found after creation."));
    }

    @Transactional
    public void createThirdSupplierOrder(IFoodCreateOrderDTO ifoodDTO) {
        Company company = ifoodDTO.company();
        Shift currentShift = verificationsServices.retrieveCurrentShift(company);

        Order order = new Order(currentShift, (currentShift.getOrders().size() + 1), ifoodDTO);
        Order orderCreated = orderRepo.save(order);

        List<OrdersItems> ordersItems = mapOrderItems(orderCreated, ifoodDTO.orderItemsDTOs(), company, ifoodDTO);
        orderCreated.setOrderItems(ordersItems);
        orderRepo.save(orderCreated);
    }

    public Order addNotesOnOrder(String requesterID, UpdateNotesOnOrderDTO notesAndOrderID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(notesAndOrderID.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Shift currentShift = verificationsServices.retrieveCurrentShift(company);

        Order order = currentShift.getOrders().stream().filter(x -> x.getId().equals(notesAndOrderID.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN) throw new RuntimeException("Can't add notes to no open orders.");

        order.setNotes(notesAndOrderID.notes());

        signalR.sendShiftOperationSigr(company);
        return orderRepo.save(order);
    }

    public Order addProductsOnOrder(String requesterID, ProductsToAddOnOrderDTO productsToAdd) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(productsToAdd.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, productsToAdd.orderID());
        if (order.getStatus() != OrderStatus.OPEN)
            throw new RuntimeException("Can't add orderItemsIDs to no open orders.");

        List<OrdersItems> ordersItems = mapOrderItems(order, productsToAdd.orderItemsIDs(), company, null);

        order.getOrderItems().addAll(ordersItems);
        calculateTotalPriceTaxAndDiscount(company, order, null);
        orderRepo.save(order);
        createPrintSyncTable(company, order, ordersItems, "add");

        signalR.sendShiftOperationSigr(company);
        return orderRepo.findById(order.getId()).orElseThrow(() -> new RuntimeException("Order not found after adding orderItemsIDs."));
    }

    public Order removeProductsOnOrder(String requesterID, ProductsToRemoveOnOrderDTO productsToRemove) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(productsToRemove.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, productsToRemove.orderID());
        if (order.getStatus() != OrderStatus.OPEN) throw new RuntimeException("Can't remove Items to no open orders.");

        List<OrdersItems> ordersItemsToCancel = ordersItemsRepo.findAllById(productsToRemove.ordersItemsIDs());
        ordersItemsToCancel.forEach(x -> x.setStatus("CANCELLED"));
        ordersItemsRepo.saveAll(ordersItemsToCancel);

        calculateTotalPriceTaxAndDiscount(company, order, null);
        orderRepo.save(order);
        createPrintSyncTable(company, order, ordersItemsToCancel, "del");

        signalR.sendShiftOperationSigr(company);
        return orderRepo.findById(order.getId()).orElseThrow(() -> new RuntimeException("Order not found after removing orderItemsIDs."));
    }

    public Order updateOrder(String requesterID, ChangeOrderTableDTO changeOrderTableDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(changeOrderTableDTO.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, changeOrderTableDTO.orderID());
        if (order.getIsThirdSpOrder() != null) throw new RuntimeException("Cannot update third party supplier orders.");

        if (!order.getStatus().equals(OrderStatus.OPEN))
            throw new RuntimeException("toUpdateOrderReopenFirst");

        Customer customer = changeOrderTableDTO.customerID() != null ? findCustomerOnCompany(company, changeOrderTableDTO.customerID()) : order.getCustomer();
        String pickUpName = changeOrderTableDTO.pickupName() != null ? changeOrderTableDTO.pickupName() : order.getPickupName();

        if (changeOrderTableDTO.tableNumberOrDeliveryOrPickup().equals("delivery")) {
            deliveryVerifications(company, customer, changeOrderTableDTO.deliveryDistanceKM());

            order.setDeliveryTax(customerDeliveryFeePlusExtraFee(company, customer));
            order.setCustomer(customer);
            order.setPickupName(null);
            order.setTableNumberOrDeliveryOrPickup("delivery");

        } else if (changeOrderTableDTO.tableNumberOrDeliveryOrPickup().equals("pickup")) {
            pickUpVerifications(customer, pickUpName);

            order.setPickupName(changeOrderTableDTO.pickupName());
            order.setCustomer(customer);
            order.setDeliveryTax(0.0);
            order.setTableNumberOrDeliveryOrPickup("pickup");

        } else {
            int newTableNumber = isTableAvailable(company, changeOrderTableDTO.tableNumberOrDeliveryOrPickup(), order);
            order.setTableNumberOrDeliveryOrPickup(String.valueOf(newTableNumber));
            order.setDeliveryTax(0.0);

            order.setCustomer(customer);
            order.setPickupName(pickUpName);
        }

        order.setNotes(changeOrderTableDTO.notes());
        orderRepo.save(order);

        signalR.sendShiftOperationSigr(company);
        return orderRepo.save(order);
    }

    public void closeOrder(String requesterID, OrderToCloseDTO orderToCloseDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(orderToCloseDTO.companyID());
        verificationsServices.worksOnCompany(company, requester);

        orderToCloseDTO.ordersIDs().forEach(x -> {
            Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, x);

            if (order.getStatus() != OrderStatus.OPEN && order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT) return;

            if (order.getTableNumberOrDeliveryOrPickup().equals("delivery")) {
                order.setDeliveryManID(orderToCloseDTO.deliverymanID());
                order.setDeliveryOrdersSequence(orderToCloseDTO.deliveryOrdersSequence());
            }

            if (order.getTableNumberOrDeliveryOrPickup().equals("pickup")) {
                order.setDeliveryTax(0.0);
            }

            if (!order.getTableNumberOrDeliveryOrPickup().equals("delivery") && !order.getTableNumberOrDeliveryOrPickup().equals("pickup")) {
                order.setDeliveryTax(0.0);
            }

            calculateTotalPriceTaxAndDiscount(company, order, orderToCloseDTO);
            order.setStatus(OrderStatus.CLOSEDWAITINGPAYMENT);
            order.setClosedWaitingPaymentAtUtc(LocalDateTime.now(ZoneOffset.UTC));
            order.setCompletedByUser(requester);
            orderRepo.save(order);
        });

//        signalR.sendShiftOperationSigr(company);
    }

    public Order confirmPaidOrder(String requesterID, FindOrderDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, dto.orderID());

        if (order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT)
            throw new RuntimeException("Only orders with status 'CLOSEDWAITINGPAYMENT' can be confirmed as paid.");

        order.setStatus(OrderStatus.PAID);
        order.setCompletedByUser(requester);
        order.setCompletedOrderDateUtc(LocalDateTime.now(ZoneOffset.UTC));

        signalR.sendShiftOperationSigr(company);
        return orderRepo.save(order);
    }

    public void reopenOrder(String requesterID, ReopenOrdersDTO orderToReopen) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(orderToReopen.companyID());
        verificationsServices.worksOnCompany(company, requester);

        orderToReopen.ordersIDs().forEach(x -> {
            Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, x);

            if (order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT) return;

            order.setServiceTax(0);
            order.setDiscount(0);
            order.setTotalPrice(0);
            order.setStatus(OrderStatus.OPEN);
            order.setCompletedByUser(null);
            order.setDeliveryManID(null);
            order.setDeliveryOrdersSequence(null);

            orderRepo.save(order);
        });

        signalR.sendShiftOperationSigr(company);
    }

    public Order cancelOrder(String requesterID, ConfirmOrCancelOrderDTO cancelOrderDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(cancelOrderDTO.companyID());
        AuthUserLogin manager = authUserRepository.findById(cancelOrderDTO.managerID()).orElseThrow(() -> new RuntimeException("Manager not found"));
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        List<Order> orderOpened = orderRepo.findByStatusInAndShift_Company(List.of(OrderStatus.OPEN, OrderStatus.CLOSEDWAITINGPAYMENT), company);
        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, cancelOrderDTO.orderID());
        if (order.getIsThirdSpOrder() != null) throw new RuntimeException("Cannot cancel third party supplier orders.");

        if (order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT && order.getStatus() != OrderStatus.OPEN)
            throw new RuntimeException("Only orders with status 'OPEN or CLOSEDWAITINGPAYMENT' can be cancelled.");

        if (new BCryptPasswordEncoder().matches(cancelOrderDTO.adminPassword(), manager.getOwnAdministrativePassword())) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCompletedByUser(requester);
            order.setIfCanceledAuthorizedByUser(manager);
            order.setNotes((order.getNotes() != null ? order.getNotes() + " \n | " : "") + "Cancellation Reason: " + cancelOrderDTO.cancellationReason());
            order.setCompletedOrderDateUtc(LocalDateTime.now(ZoneOffset.UTC));

//            signalR.sendShiftOperationSigr(company);
            return orderRepo.save(order);
        } else {
            throw new RuntimeException("Invalid admin password.");
        }
    }

    // <> ---------- Aux Methods ---------- <>

    // <>---------------------------- ADD/REMOVE ITEMS HELPERS -----------------------------------<>
    private Map<UUID, Product> getProductMap(Company company) {
        return company.getProductsCategories().stream()
                .flatMap(c -> c.getProducts().stream())
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    private Map<UUID, ProductOption> getProductOptsMap(Company company) {
        return company.getProductsCategories().stream()
                .flatMap(c -> c.getProductOptions().stream())
                .collect(Collectors.toMap(ProductOption::getId, p -> p));
    }

    private List<OrdersItems> mapOrderItems(Order order, List<OrderItemDTO> orderItemsToAddIDs, Company company, IFoodCreateOrderDTO thirdSpData) {
        List<OrdersItems> ordersItems = new ArrayList<>();
        Map<UUID, Product> productMap = getProductMap(company);
        Map<UUID, ProductOption> productOptsMap = getProductOptsMap(company);

        if (orderItemsToAddIDs != null) {
            orderItemsToAddIDs.forEach(x -> {
                if ((x.productsIDs() == null || x.productsIDs().isEmpty()) && thirdSpData == null)
                    throw new RuntimeException("Products are required to add order items.");

                List<Product> products = x.productsIDs().stream().map(id -> productMap.get(UUID.fromString(id))).toList();
                if (products.stream().anyMatch(Objects::isNull))
                    throw new RuntimeException("Product not found: " + x.productsIDs());

                List<ProductOption> productOptions = x.productOptsIDs() != null ?
                        x.productOptsIDs().stream().map(id -> productOptsMap.get(UUID.fromString(id))).toList() : new ArrayList<>();
                if (productOptions.stream().anyMatch(Objects::isNull))
                    throw new RuntimeException("Product Option not found: " + x.productOptsIDs());

                Double productPrice = 0.0;
                Double totalPrice;
                if (products.size() <= 1 && thirdSpData == null) {
                    productPrice = products.get(0).getPrice();
                } else {
                    if (thirdSpData == null)
                        productPrice = products.get(0).getProductCategory().getCustomOrderPriceRule().equals(CustomOrderPriceRule.BIGGESTPRICE) ?
                                products.stream().mapToDouble(Product::getPrice).max().orElse(0.0) : products.stream().mapToDouble(Product::getPrice).average().orElse(0.0);
                }

                if (thirdSpData != null && x.customPrice() != null) {
                    productPrice = x.customPrice();
                    totalPrice = x.customPrice();
                } else {
                    totalPrice = productPrice + productOptions.stream().mapToDouble(ProductOption::getPrice).sum();
                }

                List<String> productOpts = productOptions.stream().map(po -> po.getId().toString() + "|" + po.getName() + " R$ " + po.getPrice()).sorted().toList();

                String notes = x.notes();
                if (thirdSpData != null && x.ifoodPdvCodeError() != null)
                    notes = x.ifoodPdvCodeError() + (x.notes() != null ? " \n" + x.notes() : "");

                ordersItems.add(new OrdersItems(order, products, productPrice, totalPrice, productOpts, notes));
            });
        }

        ordersItemsRepo.saveAll(ordersItems);
//        printSyncService.save(new PrintSync(order, ordersItems, "add"));
        return ordersItems;
    }

    private void calculateTotalPriceTaxAndDiscount(Company company, Order order, OrderToCloseDTO orderToCloseDTO) {
        if (order.getIsThirdSpOrder() != null) return;
        order.setPrice(0.0);

        order.getOrderItems().stream().filter(x -> x.getStatus().equals("ACTIVE" +
                "" +
                "")).forEach(product -> {
            order.setPrice(order.getPrice() + (product.getPrice()));
        });

        if (orderToCloseDTO != null) {
            if (thisServiceHasTaxOrNot(company, order.getTableNumberOrDeliveryOrPickup()) && !orderToCloseDTO.clientSaidNoTax()) {
                order.setServiceTax(order.getPrice() * company.getTaxServicePercentage() / 100);
            }

            if (orderToCloseDTO.discountValue() != null) {
                order.setDiscount(-Math.abs(orderToCloseDTO.discountValue()));
            }

            order.setTotalPrice(order.getPrice() + order.getServiceTax() + order.getDiscount() + order.getDeliveryTax());
        }
    }

    private Boolean thisServiceHasTaxOrNot(Company company, String tableNumberOrDeliveryOrPickup) {

        if (tableNumberOrDeliveryOrPickup.equals("delivery") && company.getDeliveryHasServiceTax().equals(false)) {
            return false;
        } else if (tableNumberOrDeliveryOrPickup.equals("pickup") && company.getPickupHasServiceTax().equals(false)) {
            return false;
        } else {
            return true;
        }
    }

    public Customer findCustomerOnCompany(Company company, UUID customerID) {
        return company.getCustomers().stream()
                .filter(c -> c.getId().equals(customerID))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Customer not found in the company."));
    }

    // <>---------------------------- CREATE/UPDATE ORDERS HELPERS -----------------------------------<>
    private void deliveryVerifications(Company company, Customer customer, Integer deliveryDistanceKmFromDTO) {
        if (customer == null) throw new RuntimeException("Customer is required for delivery orders.");
        if (deliveryDistanceKmFromDTO == null)
            throw new RuntimeException("Delivery tax is required for delivery orders.");
        if (deliveryDistanceKmFromDTO > company.getMaxDeliveryDistanceKM())
            throw new RuntimeException("customerExceedsMaximumDistance-" + company.getMaxDeliveryDistanceKM());
    }

    private void pickUpVerifications(Customer customer, String pickupName) {
        if (customer == null && pickupName == null && pickupName.isBlank())
            throw new RuntimeException("Pickup name or Customer is required for pickup orders.");
    }

    private Integer isTableAvailable(Company company, String newTableNumberOrDeliveryOrPickup, Order order) {
        int newTableNumber = Integer.parseInt(newTableNumberOrDeliveryOrPickup);
        if (newTableNumber > company.getNumberOfTables() || newTableNumber < 1)
            throw new RuntimeException("Invalid table number.");

        List<Order> openOrders = orderRepo.findByStatusInAndShift_Company(List.of(OrderStatus.OPEN, OrderStatus.CLOSEDWAITINGPAYMENT), company);
        if (openOrders.stream().anyMatch(o -> o.getTableNumberOrDeliveryOrPickup().equals(String.valueOf(newTableNumber)) && (o.getStatus() == OrderStatus.OPEN || o.getStatus() == OrderStatus.CLOSEDWAITINGPAYMENT))) {
            if (order != null && newTableNumberOrDeliveryOrPickup.equals(order.getTableNumberOrDeliveryOrPickup()))
                return newTableNumber;
            throw new RuntimeException("Table is already occupied.");
        }

        return newTableNumber;
    }

    // <>---------------------------- PRINT SYNC HELPERS -----------------------------------<>
    private void createPrintSyncTable(Company company, Order order, List<OrdersItems> ordersItems, String action) {
        List<PrintSync> printSyncCreate = new ArrayList<>();
//        if (company.getPrintRules().stream().filter(x -> x.getPrintCategory().equals(PrintCategory.FULLORDER) && x.getPrinterID() != null && x.getCopies() > 0).findFirst().isPresent()) {
//            printSyncCreate.add(new PrintSync(company, PrintCategory.FULLORDER, printSyncService.createPreparationItemsPrint(company, order, PrintCategory.FULLORDER, ordersItems, action.equals("del"))));
//
//        } else if (company.getPrintRules().stream().filter(x -> x.getPrintCategory().equals(PrintCategory.FOODS) && x.getPrinterID() != null && x.getCopies() > 0).findFirst().isPresent()) {
//            printSyncCreate.add(new PrintSync(company, PrintCategory.FOODS, printSyncService.createPreparationItemsPrint(company, order, PrintCategory.FOODS, ordersItems, action.equals("del"))));
//
//        } else if (company.getPrintRules().stream().filter(x -> x.getPrintCategory().equals(PrintCategory.DESSERTS) && x.getPrinterID() != null && x.getCopies() > 0).findFirst().isPresent()) {
//            printSyncCreate.add(new PrintSync(company, PrintCategory.DESSERTS, printSyncService.createPreparationItemsPrint(company, order, PrintCategory.DESSERTS, ordersItems, action.equals("del"))));
//
//        } else if (company.getPrintRules().stream().filter(x -> x.getPrintCategory().equals(PrintCategory.DRINKS) && x.getPrinterID() != null && x.getCopies() > 0).findFirst().isPresent()) {
//            printSyncCreate.add(new PrintSync(company, PrintCategory.DRINKS, printSyncService.createPreparationItemsPrint(company, order, PrintCategory.DRINKS,ordersItems, action.equals("del"))));
//
//        } else if (company.getPrintRules().stream().filter(x -> x.getPrintCategory().equals(PrintCategory.BEVERAGES) && x.getPrinterID() != null && x.getCopies() > 0).findFirst().isPresent()) {
//            printSyncCreate.add(new PrintSync(company, PrintCategory.BEVERAGES, printSyncService.createPreparationItemsPrint(company, order, PrintCategory.BEVERAGES, ordersItems, action.equals("del"))));
//
//        }

        printSyncCreate.add(new PrintSync(company, PrintCategory.FULLORDER, printSyncService.createPreparationItemsPrint(company, order, PrintCategory.BEVERAGES, ordersItems, action.equals("del"))));

        if (printSyncCreate != null) printSyncRepo.saveAll(printSyncCreate);
    }

// <>---------------------------- END || CREATE/UPDATE ORDERS HELPERS || END -----------------------------------<>

//    public void markOrderAsPrinted(String requesterID, MarkOrderPrintSyncPrintedDTO dto) {
//        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
//        PrintSync printSync = printSyncRepo.findById(dto.orderPrintSyncID())
//                .orElseThrow(() -> new RuntimeException("OrderPrintSync not found"));
//
//        Company company = printSync.getOrder().getShift().getCompany();
//        verificationsServices.worksOnCompany(company, requester);
//
//        printSync.setAlreadyPrinted(true);
//        printSyncRepo.save(printSync);
//    }
}