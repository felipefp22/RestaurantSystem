package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.MarkOrderPrintSyncPrintedDTO;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.Order.DTOs.*;
import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Order.OrderPrintSync;
import com.RestaurantSystem.Entities.Order.OrdersItems;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.*;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import com.RestaurantSystem.WebSocket.SignalR;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepo orderRepo;
    private final OrdersItemsRepo ordersItemsRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final ShiftRepo shiftRepo;
    private final VerificationsServices verificationsServices;
    private final OrderPrintSyncRepo orderPrintSyncRepo;
    private final SignalR signalR;

    public OrderService(OrderRepo orderRepo, OrdersItemsRepo ordersItemsRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo, ShiftRepo shiftRepo, VerificationsServices verificationsServices,
                        OrderPrintSyncRepo orderPrintSyncRepo, SignalR signalR) {
        this.orderRepo = orderRepo;
        this.ordersItemsRepo = ordersItemsRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.shiftRepo = shiftRepo;
        this.verificationsServices = verificationsServices;
        this.orderPrintSyncRepo = orderPrintSyncRepo;
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
        order.setDeliveryTax(calculateDeliveryTax(company, orderToCreate.deliveryDistanceKM(), orderToCreate.tableNumberOrDeliveryOrPickup()));
        Order orderCreated = orderRepo.save(order);

        List<OrdersItems> ordersItems = mapOrderItems(orderToCreate, null, company, orderCreated);

        orderCreated.getOrderItems().addAll(ordersItems);
        calculateTotalPriceTaxAndDiscount(company, order, null);
        ordersItemsRepo.saveAll(ordersItems);
        orderPrintSyncRepo.save(new OrderPrintSync(order, ordersItems, "add"));
        signalR.sendShiftOperationSigr(company);

        return orderRepo.findById(orderCreated.getId()).orElseThrow(() -> new RuntimeException("Order not found after creation."));
    }

    private Product getProductFromID(UUID productID, Company company) {
        return company.getProductsCategories().stream()
                .flatMap(c -> c.getProducts().stream())
                .filter(p -> p.getId().equals(productID))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found: " + productID));
    }

    private List<OrdersItems> mapOrderItems(CreateOrderDTO orderToCreate, ProductsToAddOnOrderDTO productsToAddOnOrder, Company company, Order orderCreated) {
        List<OrdersItems> ordersItems = new ArrayList<>();
        if (orderToCreate.orderItemsIDs() != null) {
            orderToCreate.orderItemsIDs().forEach(itemDTO -> {
                Product product = getProductFromID(itemDTO.productID(), company);
                OrdersItems ordersItem = new OrdersItems(orderCreated, product, itemDTO.quantity());
                ordersItems.add(ordersItem);
            });
        }

        if (orderToCreate.customOrderItems() != null) {
            orderToCreate.customOrderItems().forEach(customItemDTO -> {
                List<Product> products = customItemDTO.productID().stream().map(productID -> getProductFromID(UUID.fromString(productID), company)).toList();
                double totalPrice = products.stream().mapToDouble(Product::getPrice).average().orElse(0.0);
                OrdersItems ordersItem = new OrdersItems(orderCreated, products, totalPrice, customItemDTO.quantity());
                ordersItems.add(ordersItem);
            });
        }
        return ordersItems;
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
        productsToAdd.orderItemsIDs().forEach(x -> {
            if (x.quantity() <= 0)
                throw new RuntimeException("Quantity must be greater than zero for product ID: " + x.productID());
        });

        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(productsToAdd.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, productsToAdd.orderID());
        if (order.getStatus() != OrderStatus.OPEN)
            throw new RuntimeException("Can't add orderItemsIDs to no open orders.");

        // ToSync is to print
        List<OrdersItems> ordersItemsToSync = new ArrayList<>();

        productsToAdd.orderItemsIDs().forEach(x -> {
            OrdersItems existingItem = order.getOrderItems().stream()
                    .filter(y -> y.getProductId().equals(x.productID()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + x.quantity());
                ordersItemsRepo.save(existingItem);

                //Here is to print, then needs have just new add quantity, not total quantity
                ordersItemsToSync.add(new OrdersItems(existingItem, x.quantity()));
            } else {
                Product product = company.getProductsCategories().stream()
                        .flatMap(c -> c.getProducts().stream())
                        .filter(p -> p.getId().equals(x.productID()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Product not found: " + x.productID()));

                OrdersItems newItem = new OrdersItems(order, product, x.quantity());
                order.getOrderItems().add(newItem);
                ordersItemsRepo.save(newItem);

                //Here is to print, then needs have just new add quantity, not total quantity
                ordersItemsToSync.add(new OrdersItems(newItem, x.quantity()));
            }
        });

        calculateTotalPriceTaxAndDiscount(company, order, null);
        orderRepo.save(order);
        orderPrintSyncRepo.save(new OrderPrintSync(order, ordersItemsToSync, "add"));

        signalR.sendShiftOperationSigr(company);
        return orderRepo.findById(order.getId()).orElseThrow(() -> new RuntimeException("Order not found after adding orderItemsIDs."));
    }

    public Order removeProductsOnOrder(String requesterID, ProductsToAddOnOrderDTO productsToRemove) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(productsToRemove.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, productsToRemove.orderID());
        if (order.getStatus() != OrderStatus.OPEN) throw new RuntimeException("Can't remove Items to no open orders.");

        List<OrdersItems> itemsToDelete = new ArrayList<>();
        productsToRemove.orderItemsIDs().forEach(x -> {
            order.getOrderItems().forEach(y -> {
                if (!y.getProductId().equals(x.productID())) return;
                int currentQty = y.getQuantity();
                int removeQty = x.quantity();

                if (removeQty >= currentQty) {
                    itemsToDelete.add(y);
                } else {
                    y.setQuantity(currentQty - removeQty);
                    ordersItemsRepo.save(y);
                }
            });
        });

        itemsToDelete.forEach(item -> {
            order.getOrderItems().remove(item); // keep object graph consistent
            ordersItemsRepo.delete(item);
        });

        calculateTotalPriceTaxAndDiscount(company, order, null);
        orderRepo.save(order);
        orderPrintSyncRepo.save(new OrderPrintSync(order, itemsToDelete, "del"));

        signalR.sendShiftOperationSigr(company);
        return orderRepo.findById(order.getId()).orElseThrow(() -> new RuntimeException("Order not found after removing orderItemsIDs."));
    }

    public Order updateOrder(String requesterID, ChangeOrderTableDTO changeOrderTableDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(changeOrderTableDTO.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, changeOrderTableDTO.orderID());
        if (!order.getStatus().equals(OrderStatus.OPEN))
            throw new RuntimeException("toUpdateOrderReopenFirst");

        Customer customer = changeOrderTableDTO.customerID() != null ? findCustomerOnCompany(company, changeOrderTableDTO.customerID()) : order.getCustomer();
        String pickUpName = changeOrderTableDTO.pickupName() != null ? changeOrderTableDTO.pickupName() : order.getPickupName();

        if (changeOrderTableDTO.tableNumberOrDeliveryOrPickup().equals("delivery")) {
            deliveryVerifications(company, customer, changeOrderTableDTO.deliveryDistanceKM());

            order.setDeliveryTax(calculateDeliveryTax(company, changeOrderTableDTO.deliveryDistanceKM(), changeOrderTableDTO.tableNumberOrDeliveryOrPickup()));
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

    public Order closeOrder(String requesterID, OrderToCloseDTO orderToCloseDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(orderToCloseDTO.companyID());
        verificationsServices.worksOnCompany(company, requester);

        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, orderToCloseDTO.orderID());

        if (order.getStatus() != OrderStatus.OPEN && order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT)
            throw new RuntimeException("Can't close to no open orders.");

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

        signalR.sendShiftOperationSigr(company);
        return orderRepo.save(order);
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

    public Order reopenOrder(String requesterID, FindOrderDTO orderToReopen) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(orderToReopen.companyID());
        verificationsServices.worksOnCompany(company, requester);

        List<Order> orderOpened = orderRepo.findByStatusInAndShift_Company(List.of(OrderStatus.OPEN, OrderStatus.CLOSEDWAITINGPAYMENT), company);
        Order order = orderOpened.stream().filter(x -> x.getId().equals(orderToReopen.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found on that company."));

        if (order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT)
            throw new RuntimeException("Can't reopen to no \"closed waiting payment\" orders.");

        order.setServiceTax(0);
        order.setDiscount(0);
        order.setTotalPrice(0);
        order.setStatus(OrderStatus.OPEN);
        order.setCompletedByUser(null);
        order.setDeliveryManID(null);
        order.setDeliveryOrdersSequence(null);

        signalR.sendShiftOperationSigr(company);
        return orderRepo.save(order);
    }

    public Order cancelOrder(String requesterID, ConfirmOrCancelOrderDTO cancelOrderDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(cancelOrderDTO.companyID());
        AuthUserLogin manager = authUserRepository.findById(cancelOrderDTO.managerID()).orElseThrow(() -> new RuntimeException("Manager not found"));
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        Shift currentShift = verificationsServices.retrieveCurrentShift(company);

        List<Order> orderOpened = orderRepo.findByStatusInAndShift_Company(List.of(OrderStatus.OPEN, OrderStatus.CLOSEDWAITINGPAYMENT), company);
        Order order = orderOpened.stream().filter(x -> x.getId().equals(cancelOrderDTO.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found on that company."));

        if (order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT && order.getStatus() != OrderStatus.OPEN)
            throw new RuntimeException("Only orders with status 'OPEN or CLOSEDWAITINGPAYMENT' can be cancelled.");

        if (new BCryptPasswordEncoder().matches(cancelOrderDTO.adminPassword(), manager.getOwnAdministrativePassword())) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCompletedByUser(requester);
            order.setIfCanceledAuthorizedByUser(manager);
            order.setNotes((order.getNotes() != null ? order.getNotes() + " \n | " : "") + "Cancellation Reason: " + cancelOrderDTO.cancellationReason());
            order.setCompletedOrderDateUtc(LocalDateTime.now(ZoneOffset.UTC));

            signalR.sendShiftOperationSigr(company);
            return orderRepo.save(order);
        } else {
            throw new RuntimeException("Invalid admin password.");
        }
    }

    // <> ---------- Aux Methods ---------- <>
    private void calculateTotalPriceTaxAndDiscount(Company company, Order order, OrderToCloseDTO orderToCloseDTO) {
        order.setPrice(0.0);

        order.getOrderItems().forEach(product -> {
            order.setPrice(order.getPrice() + (product.getPrice() * product.getQuantity()));
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

    private Double calculateDeliveryTax(Company company, Integer deliveryDistanceKM, String tableNumberOrDeliveryOrPickup) {
        if (!tableNumberOrDeliveryOrPickup.equals("delivery")) return null;
        Double priceToSet = company.getBaseDeliveryTax();
        Integer extraKm = deliveryDistanceKM > company.getBaseDeliveryDistanceKM() ? (int) Math.ceil(deliveryDistanceKM - company.getBaseDeliveryDistanceKM()) : 0;

        priceToSet += extraKm * company.getTaxPerExtraKM();

        return priceToSet;
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
    // <>---------------------------- END || CREATE/UPDATE ORDERS HELPERS || END -----------------------------------<>

    public void markOrderAsPrinted(String requesterID, MarkOrderPrintSyncPrintedDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        OrderPrintSync orderPrintSync = orderPrintSyncRepo.findById(dto.orderPrintSyncID())
                .orElseThrow(() -> new RuntimeException("OrderPrintSync not found"));

        Company company = orderPrintSync.getOrder().getShift().getCompany();
        verificationsServices.worksOnCompany(company, requester);

        orderPrintSync.setAlreadyPrinted(true);
        orderPrintSyncRepo.save(orderPrintSync);
    }
}