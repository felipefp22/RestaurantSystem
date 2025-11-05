package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.Order.DTOs.*;
import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Order.OrdersItems;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.*;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {
    @Value("${default.tax.percentage}")
    private Double defaultTaxPercentage;

    private final OrderRepo orderRepo;
    private final OrdersItemsRepo ordersItemsRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final ShiftRepo shiftRepo;
    private final VerificationsServices verificationsServices;

    public OrderService(OrderRepo orderRepo, OrdersItemsRepo ordersItemsRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo, ShiftRepo shiftRepo, VerificationsServices verificationsServices) {
        this.orderRepo = orderRepo;
        this.ordersItemsRepo = ordersItemsRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.shiftRepo = shiftRepo;
        this.verificationsServices = verificationsServices;
    }

    // <> ---------- Methods ---------- <>

    public Order createOrder(String requesterID, CreateOrderDTO orderToCreate) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(orderToCreate.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        Customer customer = null;
        if (orderToCreate.customerID() != null) {
            customer = company.getCustomers().stream()
                    .filter(c -> c.getId().equals(orderToCreate.customerID()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Customer not found in the company."));
        }

        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
        if (openedShift.isEmpty()) {
            throw new RuntimeException("No active shift found");
        }
        Shift currentShift = null;
        if (openedShift.size() > 1) {
            Shift lastShift = openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
        } else {
            currentShift = openedShift.get(0);
        }
        ;

        if (orderToCreate.tableNumberOrDeliveryOrPickup().equals("delivery") && customer == null) {
            throw new RuntimeException("Customer is required for delivery orders.");
        }

        if (!orderToCreate.tableNumberOrDeliveryOrPickup().equals("delivery") && !orderToCreate.tableNumberOrDeliveryOrPickup().equals("pickup")) {
            isTableAvailable(company, orderToCreate.tableNumberOrDeliveryOrPickup(), null);
        }

        Order order = new Order(requester, currentShift, (currentShift.getOrders().size() + 1), orderToCreate, customer);
        Order orderCreated = orderRepo.save(order);

        List<OrdersItems> ordersItems = new ArrayList<>();
        if (orderToCreate.orderItemsIDs() != null) {
            ordersItems = orderToCreate.orderItemsIDs().stream().map(x -> {
                Product product = company.getProductsCategories().stream().flatMap(c -> c.getProducts().stream())
                        .filter(p -> p.getId().equals(x.productID()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Product not found: " + x.productID()));

                return new OrdersItems(orderCreated, product, x.quantity());
            }).toList();
        }

        orderCreated.getOrderItems().addAll(ordersItems);
        ordersItemsRepo.saveAll(ordersItems);

        return orderRepo.findById(orderCreated.getId()).orElseThrow(() -> new RuntimeException("Order not found after creation."));
    }

    public Order addNotesOnOrder(String requesterID, UpdateNotesOnOrderDTO notesAndOrderID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(notesAndOrderID.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
        if (openedShift.isEmpty()) {
            throw new RuntimeException("No active shift found");
        }
        Shift currentShift = null;
        if (openedShift.size() > 1) {
            Shift lastShift = openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
        } else {
            currentShift = openedShift.get(0);
        }
        ;

        Order order = currentShift.getOrders().stream().filter(x -> x.getId().equals(notesAndOrderID.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN) throw new RuntimeException("Can't add notes to no open orders.");

        order.setNotes(notesAndOrderID.notes());

        return orderRepo.save(order);
    }

    public Order addProductsOnOrder(String requesterID, ProductsToAddOnOrderDTO productsToAdd) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(productsToAdd.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
        if (openedShift.isEmpty()) {
            throw new RuntimeException("No active shift found");
        }
        Shift currentShift = null;
        if (openedShift.size() > 1) {
            Shift lastShift = openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
        } else {
            currentShift = openedShift.get(0);
        }
        ;

        Order order = currentShift.getOrders().stream().filter(x -> x.getId().equals(productsToAdd.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN)
            throw new RuntimeException("Can't add orderItemsIDs to no open orders.");

        productsToAdd.orderItemsIDs().forEach(x -> {
            OrdersItems existingItem = order.getOrderItems().stream()
                    .filter(y -> y.getProductId().equals(x.productID()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + x.quantity());
                ordersItemsRepo.save(existingItem);
            } else {
                Product product = company.getProductsCategories().stream()
                        .flatMap(c -> c.getProducts().stream())
                        .filter(p -> p.getId().equals(x.productID()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Product not found: " + x.productID()));

                OrdersItems newItem = new OrdersItems(order, product, x.quantity());
                order.getOrderItems().add(newItem);
                ordersItemsRepo.save(newItem);
            }
        });

        calculateTotalPriceTaxAndDiscount(order, null);
        orderRepo.save(order);

        return orderRepo.findById(order.getId()).orElseThrow(() -> new RuntimeException("Order not found after adding orderItemsIDs."));
    }

    public Order removeProductsOnOrder(String requesterID, ProductsToAddOnOrderDTO productsToRemove) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(productsToRemove.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
        if (openedShift.isEmpty()) {
            throw new RuntimeException("No active shift found");
        }

        Shift currentShift = null;
        if (openedShift.size() > 1) {
            Shift lastShift = openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
        } else {
            currentShift = openedShift.get(0);
        }
        ;

        Order order = currentShift.getOrders().stream().filter(x -> x.getId().equals(productsToRemove.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN)
            throw new RuntimeException("Can't remove orderItemsIDs to no open orders.");

        List<OrdersItems> itemsToDelete = new ArrayList<>();

        productsToRemove.orderItemsIDs().forEach(x -> {
            order.getOrderItems().forEach(y -> {
                if (y.getProductId().equals(x.productID())) {
                    if (x.quantity() >= y.getQuantity()) {
                        itemsToDelete.add(y);
                    } else {
                        y.setQuantity(y.getQuantity() - x.quantity());
                        ordersItemsRepo.save(y);
                    }
                }
            });
        });

        itemsToDelete.forEach(item -> {
            order.getOrderItems().remove(item); // keep object graph consistent
            ordersItemsRepo.delete(item);
        });

        calculateTotalPriceTaxAndDiscount(order, null);
        orderRepo.save(order);

        return orderRepo.findById(order.getId()).orElseThrow(() -> new RuntimeException("Order not found after removing orderItemsIDs."));
    }

    public Order updateOrder(String requesterID, ChangeOrderTableDTO changeOrderTableDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(changeOrderTableDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
        if (openedShift.isEmpty()) {
            throw new RuntimeException("No active shift found");
        }
        Shift currentShift = null;
        if (openedShift.size() > 1) {
            Shift lastShift = openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
        } else {
            currentShift = openedShift.get(0);
        }

        Order order = currentShift.getOrders().stream().filter(x -> x.getId().equals(changeOrderTableDTO.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN && order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT)
            throw new RuntimeException("Can't change table of no open or waiting payment orders.");

        if (changeOrderTableDTO.tableNumberOrDeliveryOrPickup().equals("delivery")) {
            if (changeOrderTableDTO.customerID() != null) {
                Customer customer = company.getCustomers().stream()
                        .filter(c -> c.getId().equals(changeOrderTableDTO.customerID()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Customer not found in the company."));

                order.setCustomer(customer);
                order.setPickupName(null);
                order.setTableNumberOrDeliveryOrPickup("delivery");
            } else {
                throw new RuntimeException("Customer is required for delivery orders.");
            }
        } else if (changeOrderTableDTO.tableNumberOrDeliveryOrPickup().equals("pickup")) {
            if (changeOrderTableDTO.pickupName() != null && !changeOrderTableDTO.pickupName().isEmpty()) {
                order.setPickupName(changeOrderTableDTO.pickupName());
                order.setCustomer(changeOrderTableDTO.customerID() != null ? company.getCustomers().stream()
                        .filter(c -> c.getId().equals(changeOrderTableDTO.customerID()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Customer not found in the company.")) : null);

                order.setTableNumberOrDeliveryOrPickup("pickup");
            } else {
                throw new RuntimeException("Pickup name is required for pickup orders.");
            }
        } else {
            int newTableNumber = isTableAvailable(company, changeOrderTableDTO.tableNumberOrDeliveryOrPickup(), order);
            order.setTableNumberOrDeliveryOrPickup(String.valueOf(newTableNumber));
            
            if (changeOrderTableDTO.customerID() != null) {
                Customer customerFound = company.getCustomers().stream()
                        .filter(c -> c.getId().equals(changeOrderTableDTO.customerID()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Customer not found in the company."));

                order.setCustomer(customerFound);
                order.setPickupName(null);
            }

            if (order.getCustomer() == null && changeOrderTableDTO.pickupName() != null) {
                order.setPickupName(changeOrderTableDTO.pickupName());
            }
        }

        order.setNotes(changeOrderTableDTO.notes());
        orderRepo.save(order);

        return orderRepo.save(order);
    }

    public Order closeOrder(String requesterID, OrderToCloseDTO orderToCloseDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(orderToCloseDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        List<Order> orderOpened = orderRepo.findByStatusInAndShift_Company(List.of(OrderStatus.OPEN, OrderStatus.CLOSEDWAITINGPAYMENT), company);
        Order order = orderOpened.stream().filter(x -> x.getId().equals(orderToCloseDTO.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found on that company."));

        if (order.getStatus() != OrderStatus.OPEN && order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT)
            throw new RuntimeException("Can't close to no open orders.");

        calculateTotalPriceTaxAndDiscount(order, orderToCloseDTO);
        order.setStatus(OrderStatus.CLOSEDWAITINGPAYMENT);
        order.setClosedWaitingPaymentAtUtc(LocalDateTime.now(ZoneOffset.UTC));
        order.setCompletedByUser(requester);

        return orderRepo.save(order);
    }

    public Order confirmPaidOrder(String requesterID, FindOrderDTO dto) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(dto.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
        if (openedShift.isEmpty()) {
            throw new RuntimeException("No active shift found");
        }

        Shift currentShift = null;
        if (openedShift.size() > 1) {
            Shift lastShift = openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
        } else {
            currentShift = openedShift.get(0);
        }
        ;

        List<Order> orderOpened = orderRepo.findByStatusInAndShift_Company(List.of(OrderStatus.OPEN, OrderStatus.CLOSEDWAITINGPAYMENT), company);
        Order order = orderOpened.stream().filter(x -> x.getId().equals(dto.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found on that company."));
        if (order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT)
            throw new RuntimeException("Can't confirm payment for no closed waiting payment orders.");

        if (order.getStatus() == OrderStatus.CLOSEDWAITINGPAYMENT) {
            order.setStatus(OrderStatus.PAID);
            order.setCompletedByUser(requester);
            order.setCompletedOrderDateUtc(LocalDateTime.now(ZoneOffset.UTC));

            return orderRepo.save(order);
        } else if (order.getStatus() == OrderStatus.PAID) {
            throw new RuntimeException("Order is already paid.");
        } else {
            throw new RuntimeException("Only orders with status 'CLOSEDWAITINGPAYMENT' can be confirmed as paid.");
        }
    }

    public Order reopenOrder(String requesterID, FindOrderDTO orderToReopen) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(orderToReopen.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
        if (openedShift.isEmpty()) {
            throw new RuntimeException("No active shift found");
        }

        List<Order> orderOpened = orderRepo.findByStatusInAndShift_Company(List.of(OrderStatus.OPEN, OrderStatus.CLOSEDWAITINGPAYMENT), company);
        Order order = orderOpened.stream().filter(x -> x.getId().equals(orderToReopen.orderID())).findFirst().orElseThrow(() -> new RuntimeException("Order not found on that company."));

        if (order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT)
            throw new RuntimeException("Can't reopen to no \"closed waiting payment\" orders.");

        order.setPrice(0);
        order.setServiceTax(0);
        order.setDiscount(0);
        order.setTotalPrice(0);
        order.setStatus(OrderStatus.OPEN);
        order.setCompletedByUser(null);

        return orderRepo.save(order);
    }

    public Order cancelOrder(String requesterID, ConfirmOrCancelOrderDTO cancelOrderDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(cancelOrderDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        AuthUserLogin manager = authUserRepository.findById(cancelOrderDTO.managerID()).orElseThrow(() -> new RuntimeException("Manager not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, manager))
            throw new RuntimeException("justOwnerManagerSupervisorCanCancelOrders");

        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
        if (openedShift.isEmpty()) {
            throw new RuntimeException("No active shift found");
        }

        Shift currentShift = null;
        if (openedShift.size() > 1) {
            Shift lastShift = openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
        } else {
            currentShift = openedShift.get(0);
        }
        ;

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

            return orderRepo.save(order);
        } else {
            throw new RuntimeException("Invalid admin password.");
        }
    }

    // <> ---------- Aux Methods ---------- <>
    private void calculateTotalPriceTaxAndDiscount(Order order, OrderToCloseDTO orderToCloseDTO) {
        order.setPrice(0.0);

        order.getOrderItems().forEach(product -> {
            order.setPrice(order.getPrice() + product.getPrice());
        });

        if (orderToCloseDTO != null) {
            order.setServiceTax(orderToCloseDTO.clientSaidNoTax() ? 0.0 : order.getPrice() * defaultTaxPercentage / 100);
            order.setDiscount(orderToCloseDTO.discountValue() != null ? -Math.abs(orderToCloseDTO.discountValue()) : 0.0);

            order.setTotalPrice(order.getPrice() + order.getServiceTax() + order.getDiscount());
        }
    }

    private Integer isTableAvailable(Company company, String newTableNumberOrDeliveryOrPickup, Order order) {
        int newTableNumber = Integer.parseInt(newTableNumberOrDeliveryOrPickup);
        if (newTableNumber > company.getNumberOfTables() || newTableNumber < 1)
            throw new RuntimeException("Invalid table number.");

        List<Order> openOrders = orderRepo.findByStatusInAndShift_Company(List.of(OrderStatus.OPEN, OrderStatus.CLOSEDWAITINGPAYMENT), company);
        if (openOrders.stream().anyMatch(o -> o.getTableNumberOrDeliveryOrPickup().equals(String.valueOf(newTableNumber)) && (o.getStatus() == OrderStatus.OPEN || o.getStatus() == OrderStatus.CLOSEDWAITINGPAYMENT))) {
            if(order != null && newTableNumberOrDeliveryOrPickup.equals(order.getTableNumberOrDeliveryOrPickup())) return newTableNumber;
            throw new RuntimeException("Table is already occupied.");
        }

        return newTableNumber;
    }
}


