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
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.OrderRepo;
import com.RestaurantSystem.Repositories.OrdersItemsRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    @Value("${default.tax.percentage}")
    private Double defaultTaxPercentage;

    @Value("${cancel.order.admin.password}")
    private String adminPassword;

    private final OrderRepo orderRepo;
    private final OrdersItemsRepo ordersItemsRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final VerificationsServices verificationsServices;

    public OrderService(OrderRepo orderRepo, OrdersItemsRepo ordersItemsRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo, VerificationsServices verificationsServices) {
        this.orderRepo = orderRepo;
        this.ordersItemsRepo = ordersItemsRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.verificationsServices = verificationsServices;
    }

    // <> ---------- Methods ---------- <>

    public Order createOrder(String requesterID, CreateOrderDTO orderToCreate) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(orderToCreate.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the categories of this company");

        Customer customer = null;
        if (orderToCreate.customerID() != null) {
            customer = company.getCustomers().stream()
                    .filter(c -> c.getId().toString().equals(orderToCreate.customerID()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Customer not found in the company."));
        }

        Shift currentShift = company.getShifts().stream()
                .filter(x -> x.getEndTimeUTC() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active shift found for the company."));

        if (orderToCreate.tableNumberOrDeliveryOrPickup().equals("delivery") && customer == null) {
            throw new RuntimeException("Customer is required for delivery orders.");
        }

        if (!orderToCreate.tableNumberOrDeliveryOrPickup().equals("delivery") && !orderToCreate.tableNumberOrDeliveryOrPickup().equals("pickup")) {
            int tableNumber = Integer.parseInt(orderToCreate.tableNumberOrDeliveryOrPickup());
            if (tableNumber > company.getNumberOfTables() || tableNumber < 1)
                throw new RuntimeException("Invalid table number.");
            if (currentShift.getOrders().stream()
                    .anyMatch(o -> o.getTableNumberOrDeliveryOrPickup().equals(orderToCreate.tableNumberOrDeliveryOrPickup()) && (o.getStatus() == OrderStatus.OPEN || o.getStatus() == OrderStatus.CLOSEDWAITINGPAYMENT))) {
                throw new RuntimeException("Table is already occupied.");
            }
        }

        Order order = new Order(currentShift, (currentShift.getOrders().size() + 1), orderToCreate, customer);
        Order orderCreated = orderRepo.save(order);

        List<OrdersItems> ordersItems = orderToCreate.orderItemsIDs().stream().map(x -> {
            Product product = company.getProductsCategories().stream().flatMap(c -> c.getProducts().stream())
                    .filter(p -> p.getId().equals(x.productID()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found: " + x.productID()));

            return new OrdersItems(orderCreated, product, x.quantity());
        }).toList();

        ordersItemsRepo.saveAll(ordersItems);

        return orderRepo.findById(orderCreated.getId()).orElseThrow(() -> new RuntimeException("Order not found after creation."));
    }

    public Order addNotesOnOrder(String requesterID, UpdateNotesOnOrderDTO notesAndOrderID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(notesAndOrderID.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the categories of this company");

        Shift currentShift = company.getShifts().stream()
                .filter(x -> x.getEndTimeUTC() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active shift found for the company."));

        Order order = currentShift.getOrders().stream().filter(x -> x.getId() == notesAndOrderID.orderId()).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN) throw new RuntimeException("Can't add notes to no open orders.");

        order.setNotes(notesAndOrderID.notes());

        return orderRepo.save(order);
    }

    public Order addProductsOnOrder(String requesterID, ProductsToAddOnOrderDTO productsToAdd) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(productsToAdd.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the categories of this company");

        Shift currentShift = company.getShifts().stream()
                .filter(x -> x.getEndTimeUTC() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active shift found for the company."));

        Order order = currentShift.getOrders().stream().filter(x -> x.getId() == productsToAdd.orderId()).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN) throw new RuntimeException("Can't add products to no open orders.");

        List<OrdersItems> ordersItems = productsToAdd.products().stream().map(x -> {
            Product product = company.getProductsCategories().stream().flatMap(c -> c.getProducts().stream())
                    .filter(p -> p.getId().equals(x.productId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Product not found: " + x.productId()));


            return new OrdersItems(order, product, x.quantity());
        }).toList();

        ordersItemsRepo.saveAll(ordersItems);

        return orderRepo.findById(order.getId()).orElseThrow(() -> new RuntimeException("Order not found after adding products."));
    }

    public Order removeProductsOnOrder(String requesterID, ProductsToAddOnOrderDTO productsToRemove) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(productsToRemove.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the categories of this company");

        Shift currentShift = company.getShifts().stream()
                .filter(x -> x.getEndTimeUTC() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active shift found for the company."));

        Order order = currentShift.getOrders().stream().filter(x -> x.getId() == productsToRemove.orderId()).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN)
            throw new RuntimeException("Can't remove products to no open orders.");

        List<OrdersItems> itemsToDelete = new ArrayList<>();

        productsToRemove.products().forEach(x -> {
            order.getOrderItems().forEach(y -> {
                if (y.getProductId().equals(x.productId())) {
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

        return orderRepo.findById(order.getId()).orElseThrow(() -> new RuntimeException("Order not found after removing products."));
    }

    public Order moveToAnotherTable(String requesterID, ChangeOrderTableDTO changeOrderTableDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(changeOrderTableDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the categories of this company");

        Shift currentShift = company.getShifts().stream()
                .filter(x -> x.getEndTimeUTC() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active shift found for the company."));

        Order order = currentShift.getOrders().stream().filter(x -> x.getId() == changeOrderTableDTO.orderID()).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN)
            throw new RuntimeException("Can't remove products to no open orders.");

        if (changeOrderTableDTO.tableNumberOrDeliveryOrPickup().equals("delivery")) {
            if (changeOrderTableDTO.customerID() != null) {
                Customer customer = company.getCustomers().stream()
                        .filter(c -> c.getId().toString().equals(changeOrderTableDTO.customerID()))
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
                        .filter(c -> c.getId().toString().equals(changeOrderTableDTO.customerID()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Customer not found in the company.")) : null);

                order.setTableNumberOrDeliveryOrPickup("pickup");
            } else {
                throw new RuntimeException("Pickup name is required for pickup orders.");
            }
        } else {
            int newTableNumber = Integer.parseInt(changeOrderTableDTO.tableNumberOrDeliveryOrPickup());
            if (newTableNumber > company.getNumberOfTables() || newTableNumber < 1)
                throw new RuntimeException("Invalid table number.");
            if (currentShift.getOrders().stream()
                    .anyMatch(o -> o.getTableNumberOrDeliveryOrPickup().equals(String.valueOf(newTableNumber)) && (o.getStatus() == OrderStatus.OPEN || o.getStatus() == OrderStatus.CLOSEDWAITINGPAYMENT))) {
                throw new RuntimeException("Table is already occupied.");
            }

            order.setTableNumberOrDeliveryOrPickup(String.valueOf(newTableNumber));
            order.setCustomer(changeOrderTableDTO.customerID() != null ? company.getCustomers().stream()
                    .filter(c -> c.getId().toString().equals(changeOrderTableDTO.customerID()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Customer not found in the company.")) : null);

            order.setPickupName(null);
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

        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the categories of this company");

        Shift currentShift = company.getShifts().stream()
                .filter(x -> x.getEndTimeUTC() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active shift found for the company."));

        Order order = currentShift.getOrders().stream().filter(x -> x.getId() == orderToCloseDTO.orderID()).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN) throw new RuntimeException("Can't close to no open orders.");

        calculateTotalPriceTaxAndDiscount(order, orderToCloseDTO);
        order.setStatus(OrderStatus.CLOSEDWAITINGPAYMENT);
        order.setCompletedByUser(requester);

        return orderRepo.save(order);
    }

    public Order confirmPaidOrder(String requesterID, FindOrderDTO dto) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(dto.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the categories of this company");

        Shift currentShift = company.getShifts().stream()
                .filter(x -> x.getEndTimeUTC() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active shift found for the company."));

        Order order = currentShift.getOrders().stream().filter(x -> x.getId() == dto.orderID()).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN) throw new RuntimeException("Can't close to no open orders.");

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

    public Order cancelOrder(String requesterID, ConfirmOrCancelOrderDTO cancelOrderDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(cancelOrderDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        AuthUserLogin manager = authUserRepository.findById(cancelOrderDTO.managerID()).orElseThrow(() -> new RuntimeException("Manager not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, manager)) throw new RuntimeException("You are not allowed to see the categories of this company");

        Shift currentShift = company.getShifts().stream()
                .filter(x -> x.getEndTimeUTC() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active shift found for the company."));

        Order order = currentShift.getOrders().stream().filter(x -> x.getId() == cancelOrderDTO.orderID()).findFirst().orElseThrow(() -> new RuntimeException("Order not found in the current shift."));
        if (order.getStatus() != OrderStatus.OPEN) throw new RuntimeException("Can't close to no open orders.");
        if (order.getStatus() != OrderStatus.CLOSEDWAITINGPAYMENT)
            throw new RuntimeException("Only orders with status 'CLOSEDWAITINGPAYMENT' can be cancelled.");


        if (new BCryptPasswordEncoder().matches(cancelOrderDTO.adminPassword(), adminPassword)) {
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
    public void calculateTotalPriceTaxAndDiscount(Order order, OrderToCloseDTO orderToCloseDTO) {
        order.setPrice(0.0);

        order.getOrderItems().forEach(product -> {
            order.setPrice(order.getPrice() + product.getPrice());
        });

        order.setServiceTax(orderToCloseDTO.clientSaidNoTax() ? 0.0 : order.getPrice() * defaultTaxPercentage);
        order.setDiscount(orderToCloseDTO.discountValue() != null ? -Math.abs(orderToCloseDTO.discountValue()) : 0.0);

        order.setTotalPrice(order.getPrice() + order.getServiceTax() + order.getDiscount());
    }
}


