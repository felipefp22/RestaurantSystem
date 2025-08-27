package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import com.RestaurantSystem.Entities.Order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepo extends JpaRepository<Order, UUID> {

    List<Order> findAllByTableNumberAndStatus(int tableNumber, OrderStatus orderStatus);
}
