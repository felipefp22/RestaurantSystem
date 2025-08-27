package com.RestaurantSystem.Entities.Order.DTOs;
import com.RestaurantSystem.Entities.Product.Product;

import java.util.List;

public record CreateOrderDTO(
        int tableNumber,
        List<Product> orderItems,
        String notes
) {
}
