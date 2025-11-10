package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Order.OrderPrintSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderPrintSyncRepo extends JpaRepository<OrderPrintSync, UUID> {

}
