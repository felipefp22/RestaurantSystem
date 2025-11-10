package com.RestaurantSystem.Entities.Order;

import java.util.UUID;

public record OrdersItemsPrintSync(
        UUID productId,
        String name,
        int quantity
) {
    public OrdersItemsPrintSync(OrdersItems ordersItems) {
        this(
                ordersItems.getProductId(),
                ordersItems.getName(),
                ordersItems.getQuantity()
        );
    }
}
