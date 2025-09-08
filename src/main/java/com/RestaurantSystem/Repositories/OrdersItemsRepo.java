package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Order.OrdersItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrdersItemsRepo extends JpaRepository<OrdersItems, UUID> {

}
