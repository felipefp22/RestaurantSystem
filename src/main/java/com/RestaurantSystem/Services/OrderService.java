package com.RestaurantSystem.Services;

import com.RestaurantSystem.Repositories.OrderRepo;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepo orderRepo;

    public OrderService(OrderRepo orderRepo) {
        this.orderRepo = orderRepo;
    }

    // <> ---------- Methods ---------- <>
}
