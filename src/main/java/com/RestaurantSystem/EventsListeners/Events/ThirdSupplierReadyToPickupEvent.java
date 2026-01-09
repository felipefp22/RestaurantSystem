package com.RestaurantSystem.EventsListeners.Events;

import com.RestaurantSystem.Entities.Order.Order;

public class ThirdSupplierReadyToPickupEvent {
    private final Order order;

    // <>------------ Constructors ------------<>
    public ThirdSupplierReadyToPickupEvent(Order order) {
        this.order = order;
    }


    // <>------------ Getters ------------<>

    public Order getOrder() {
        return order;
    }
}
