package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Order.OrdersItems;
import com.RestaurantSystem.Entities.Order.OrdersItemsCancelled;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrdersItemsCancelledRepo extends JpaRepository<OrdersItemsCancelled, UUID> {

}
