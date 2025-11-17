package com.RestaurantSystem.Entities.Order.DTOs;

import com.RestaurantSystem.Entities.Order.OrdersItems;

import java.util.List;

public record OrdersItemsPrintSyncDTO(
        List<String> productId,
        String name
) {
    public OrdersItemsPrintSyncDTO(OrdersItems ordersItems) {
        this(
                ordersItems.getProductId(),
                ordersItems.getName()
        );
    }
}