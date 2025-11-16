package com.RestaurantSystem.Entities.Order;

import java.util.List;
import java.util.UUID;

public record OrdersItemsPrintSync(
        List<String> productId,
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
