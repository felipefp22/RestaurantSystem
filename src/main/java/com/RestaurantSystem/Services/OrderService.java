package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Order.DTOs.ConfirmOrCancelOrderDTO;
import com.RestaurantSystem.Entities.Order.DTOs.CreateOrderDTO;
import com.RestaurantSystem.Entities.Order.DTOs.OrderToCloseDTO;
import com.RestaurantSystem.Entities.Order.DTOs.ProductsToAddOnOrderDTO;
import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Repositories.OrderRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    @Value("${default.tax.percentage}")
    private Double defaultTaxPercentage;

    @Value("${cancel.order.admin.password}")
    private String adminPassword;

    private final OrderRepo orderRepo;

    public OrderService(OrderRepo orderRepo) {
        this.orderRepo = orderRepo;
    }

    // <> ---------- Methods ---------- <>

    public List<Order> getOrdersByDate(LocalDateTime date) {


    }

    public Object createOrder(CreateOrderDTO orderToCreate) {
    }

    public Order addNotesOnOrder(UUID uuid, String notes) {
    }

    public Order addProductsOnOrder(ProductsToAddOnOrderDTO products) {
    }

    public Order removeProductsOnOrder(ProductsToAddOnOrderDTO products) {
    }

    public Order moveToAnotherTable(UUID uuid, int tableNumber) {
        List<Order> openedOrders = orderRepo.findAllByTableNumberAndStatus(tableNumber, OrderStatus.OPEN);
        Order order = orderRepo.findById(uuid).orElseThrow(() -> new RuntimeException("Order not found."));

        if (openedOrders.size() == 0) {
            order.setTableNumberOrDeliveryOrToGo(tableNumber);
            return orderRepo.save(order);
        } else {
            throw new RuntimeException("Table is already occupied.");
        }

    }

    public Order closeOrder(OrderToCloseDTO orderToCloseDTO) {
        Order order = orderRepo.findById(orderToCloseDTO.orderID()).orElseThrow(() -> new RuntimeException("Order not found."));

        if (order.getStatus() == OrderStatus.OPEN) {
            calculateTotalPriceTaxAndDiscount(order, orderToCloseDTO);
            order.setStatus(OrderStatus.CLOSEDWAITINGPAYMENT);

            return orderRepo.save(order);
        } else if (order.getStatus() == OrderStatus.CLOSEDWAITINGPAYMENT) {
            throw new RuntimeException("Order is already closed waiting payment.");
        } else if (order.getStatus() == OrderStatus.PAID) {
            throw new RuntimeException("Order is already paid.");
        } else {
            throw new RuntimeException("Error.");
        }
    }

    public Order confirmPaidOrder(ConfirmOrCancelOrderDTO confirmOrderDTO) {
        Order order = orderRepo.findById(confirmOrderDTO.orderID()).orElseThrow(() -> new RuntimeException("Order not found."));

        if (order.getStatus() == OrderStatus.CLOSEDWAITINGPAYMENT) {
            order.setStatus(OrderStatus.PAID);
            return orderRepo.save(order);
        } else if (order.getStatus() == OrderStatus.CANCELLED) {
            if(confirmOrderDTO.adminPassword().equals(adminPassword)) {
                order.setStatus(OrderStatus.PAID);
            } else {
                throw new RuntimeException("Admin Password Wrong.");
            }
        }else if (order.getStatus() == OrderStatus.PAID) {
            throw new RuntimeException("Order is already paid.");
        } else if (order.getStatus() == OrderStatus.OPEN) {
            throw new RuntimeException("Order is Open, you need to close it first.");
        } else {
            throw new RuntimeException("Error.");
        }
    }

    public Order cancelOrder(ConfirmOrCancelOrderDTO cancelOrderDTO) {
        Order order = orderRepo.findById(cancelOrderDTO.orderID()).orElseThrow(() -> new RuntimeException("Order not found."));

        if (order.getStatus() == OrderStatus.OPEN || order.getStatus() == OrderStatus.CLOSEDWAITINGPAYMENT || order.getStatus() == OrderStatus.PAID) {
            if (cancelOrderDTO.adminPassword().equals(adminPassword)) {
                order.setStatus(OrderStatus.CANCELLED);

                return orderRepo.save(order);
            } else {
                throw new RuntimeException("Password Wrong.");
            }
        } else if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled.");
        } else {
            throw new RuntimeException("Error.");
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


