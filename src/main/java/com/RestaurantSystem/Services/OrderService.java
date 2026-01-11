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
import com.RestaurantSystem.EventsListeners.Events.ThirdSupplierDeliveredEvent;
import com.RestaurantSystem.EventsListeners.Events.ThirdSupplierDispatchEvent;
import com.RestaurantSystem.EventsListeners.Events.ThirdSupplierReadyToPickupEvent;
import com.RestaurantSystem.Repositories.*;
import com.RestaurantSystem.Services.AuxsServices.PrintSyncService;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import com.RestaurantSystem.WebSocket.SignalR;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;


    public OrderService(OrderRepo orderRepo, OrdersItemsRepo ordersItemsRepo, OrdersItemsCancelledRepo ordersItemsCancelledRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo,
                        ShiftRepo shiftRepo, VerificationsServices verificationsServices, PrintSyncService printSyncService, PrintSyncRepo printSyncRepo, SignalR signalR, ApplicationEventPublisher eventPublisher) {
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
        this.eventPublisher = eventPublisher;
    }

    // <> ---------- Methods ---------- <>

    @Transactional
    public Order createOrder(String requesterID, CreateOrderDTO toCreateDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(toCreateDTO.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Customer customer = toCreateDTO.customerID() != null ? findCustomerOnCompany(company, toCreateDTO.customerID()) : null;
        Shift currentShift = verificationsServices.retrieveCurrentShift(company);

        if (toCreateDTO.tableNumberOrDeliveryOrPickup().equals("delivery"))
            deliveryVerifications(company, customer);
        if (toCreateDTO.tableNumberOrDeliveryOrPickup().equals("pickup"))
            pickUpVerifications(customer, toCreateDTO.pickupName());

        if (!toCreateDTO.tableNumberOrDeliveryOrPickup().equals("delivery") && !toCreateDTO.tableNumberOrDeliveryOrPickup().equals("pickup"))
            isTableAvailable(company, toCreateDTO.tableNumberOrDeliveryOrPickup(), null);

        Order order = new Order(requester, currentShift, (currentShift.getOrders().size() + 1), toCreateDTO, customer);
        if (order.getTableNumberOrDeliveryOrPickup().equals("delivery"))
            order.setDeliveryTax(customerDeliveryFeePlusExtraFee(company, customer));
        Order orderCreated = orderRepo.save(order);

        List<OrdersItems> ordersItems = mapOrderItems(orderCreated, toCreateDTO.orderItemsIDs(), company, null);

        orderCreated.setOrderItems(ordersItems);
        if (toCreateDTO.discountValue() != null) orderCreated.setDiscount(toCreateDTO.discountValue());
        calculateTotalPriceTaxAndDiscount(company, order, null);

        if (toCreateDTO.money() != null) orderCreated.setMoney(toCreateDTO.money());
        if (toCreateDTO.pix() != null) orderCreated.setPix(toCreateDTO.pix());
        if (toCreateDTO.debit() != null) orderCreated.setDebit(toCreateDTO.debit());
        if (toCreateDTO.credit() != null) orderCreated.setCredit(toCreateDTO.credit());
        if (toCreateDTO.valeRefeicao() != null) orderCreated.setValeRefeicao(toCreateDTO.valeRefeicao());
        if (toCreateDTO.othersPaymentModes() != null)
            orderCreated.setOthersPaymentModes(toCreateDTO.othersPaymentModes());

        orderRepo.save(orderCreated);
        if (order.getTableNumberOrDeliveryOrPickup().equals("pickup") || order.getTableNumberOrDeliveryOrPickup().equals("delivery")) {
            createPrintDispatchAndPreparation(company, orderCreated, ordersItems, "add", false);
        } else {
            createPrintJustPreparation(company, orderCreated, ordersItems, "add");
        }

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
        createPrintDispatchAndPreparation(company, orderCreated, ordersItems, "add", false);
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

        if (order.getTableNumberOrDeliveryOrPickup().equals("pickup") || order.getTableNumberOrDeliveryOrPickup().equals("delivery")) {
            createPrintDispatchAndPreparation(company, order, order.getOrderItems(), "add", true);
        } else {
            createPrintJustPreparation(company, order, ordersItems, "add");
        }

        
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
        if (order.getTableNumberOrDeliveryOrPickup().equals("pickup") || order.getTableNumberOrDeliveryOrPickup().equals("delivery")) {
            createPrintDispatchAndPreparation(company, order, order.getOrderItems(), "add", true);
        } else {
            createPrintJustPreparation(company, order, ordersItemsToCancel, "del");
        }

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
            deliveryVerifications(company, customer);

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

            if (orderToCloseDTO.discountValue() != null && orderToCloseDTO.discountValue() > 0)
                order.setDiscount(orderToCloseDTO.discountValue());

            calculateTotalPriceTaxAndDiscount(company, order, orderToCloseDTO);
            order.setStatus(OrderStatus.CLOSEDWAITINGPAYMENT);
            order.setClosedWaitingPaymentAtUtc(LocalDateTime.now(ZoneOffset.UTC));
            order.setCompletedByUser(requester);
            orderRepo.save(order);
            adviseThirdSpDispatchOrReadyToPickup(order);

            if (!order.getTableNumberOrDeliveryOrPickup().equals("pickup") && !order.getTableNumberOrDeliveryOrPickup().equals("delivery")) {
                createPrintBill(company, order);
            }
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

        if (dto.money() != null) order.setMoney(dto.money());
        if (dto.pix() != null) order.setPix(dto.pix());
        if (dto.debit() != null) order.setDebit(dto.debit());
        if (dto.credit() != null) order.setCredit(dto.credit());
        if (dto.valeRefeicao() != null) order.setValeRefeicao(dto.valeRefeicao());
        if (dto.othersPaymentModes() != null) order.setOthersPaymentModes(dto.othersPaymentModes());

        signalR.sendShiftOperationSigr(company);
        adviseThirdSpDelivered(order);

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

    public void reprintOrder(String requesterID, FindOrderDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrServer(company, requester);

        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, dto.orderID());

        if (order.getTableNumberOrDeliveryOrPickup().equals("pickup") || order.getTableNumberOrDeliveryOrPickup().equals("delivery")) {
            createPrintDispatchAndPreparation(company, order, order.getOrderItems(), "add", false);
        } else {
            if (order.getStatus().equals(OrderStatus.OPEN)) {
                createPrintJustPreparation(company, order, order.getOrderItems(), "add");
            } else {
                createPrintBill(company, order);
            }
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

                List<String> productOpts = productOptions.stream().map(po -> po.getId().toString() + "|" + po.getName()).sorted().toList();

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
        }

        double deliveryTax = order.getDeliveryTax() != null ? order.getDeliveryTax() : 0;

        order.setTotalPrice(order.getPrice() + order.getServiceTax() + -Math.abs(order.getDiscount()) + deliveryTax);
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

    private void adviseThirdSpDispatchOrReadyToPickup(Order order) {
        if (order.getIsThirdSpOrder() != null) {
            if (order.getTableNumberOrDeliveryOrPickup().equals("delivery")) {
                eventPublisher.publishEvent(new ThirdSupplierDispatchEvent(order));
            }
            if (order.getTableNumberOrDeliveryOrPickup().equals("pickup")) {
                eventPublisher.publishEvent(new ThirdSupplierReadyToPickupEvent(order));
            }
        }
    }

    private void adviseThirdSpDelivered(Order order) {
        if (order.getIsThirdSpOrder() != null) {
            eventPublisher.publishEvent(new ThirdSupplierDeliveredEvent(order));
        }
    }

    // <>---------------------------- CREATE/UPDATE ORDERS HELPERS -----------------------------------<>
    private void deliveryVerifications(Company company, Customer customer) {
        if (customer == null) throw new RuntimeException("Customer is required for delivery orders.");
        if (customer.getDistanceFromStoreKM() == null || customer.getDistanceFromStoreKM() < 1)
            throw new RuntimeException("Delivery tax is required for delivery orders.");
        if (customer.getDistanceFromStoreKM() > company.getMaxDeliveryDistanceKM())
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
    private void createPrintDispatchAndPreparation(Company company, Order order, List<OrdersItems> ordersItems, String action, Boolean isEdited) {
        try {
            String dispatchString = printSyncService.createDispatchItemsPrint(company, order, PrintCategory.BEVERAGES, ordersItems, true, true, isEdited);
            String preparationString = printSyncService.createPreparationItemsPrint(company, order, PrintCategory.BEVERAGES, ordersItems, action.equals("del"), isEdited);
            String printFinalString = dispatchString + "\n\n" + printSyncService.getCutCommand() + preparationString;

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

            printSyncCreate.add(new PrintSync(company, PrintCategory.FULLORDER, printFinalString));

            if (printSyncCreate != null) printSyncRepo.saveAll(printSyncCreate);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void createPrintJustPreparation(Company company, Order order, List<OrdersItems> ordersItems, String action) {
        try {
            String preparationString = printSyncService.createPreparationItemsPrint(company, order, PrintCategory.BEVERAGES, ordersItems, action.equals("del"), false);
            String printFinalString = preparationString + "\n\n" + printSyncService.getCutCommand() + preparationString;

            List<PrintSync> printSyncCreate = new ArrayList<>();
            printSyncCreate.add(new PrintSync(company, PrintCategory.FULLORDER, printFinalString));

            if (printSyncCreate != null) printSyncRepo.saveAll(printSyncCreate);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void createPrintBill(Company company, Order order) {
        try {
            String dispatchString = printSyncService.createDispatchItemsPrint(company, order, PrintCategory.BEVERAGES, order.getOrderItems(), false, true, false);
            String printFinalString = dispatchString;

            List<PrintSync> printSyncCreate = new ArrayList<>();
            printSyncCreate.add(new PrintSync(company, PrintCategory.BILL, printFinalString));

            if (printSyncCreate != null) printSyncRepo.saveAll(printSyncCreate);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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