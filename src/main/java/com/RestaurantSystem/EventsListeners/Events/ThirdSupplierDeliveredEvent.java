package com.RestaurantSystem.EventsListeners.Events;

import com.RestaurantSystem.Entities.Order.Order;

public class ThirdSupplierDeliveredEvent {
    private final Order order;

    // <>------------ Constructors ------------<>
    public ThirdSupplierDeliveredEvent(Order order) {
        this.order = order;
    }


    // <>------------ Getters ------------<>

    public Order getOrder() {
        return order;
    }
}
