package com.RestaurantSystem.Entities.DTOs;
import com.RestaurantSystem.Entities.Product;

import java.util.List;

public record CreateOrderDTO(
        int tableNumber,
        List<Product> orderItems,
        String notes
) {
}
