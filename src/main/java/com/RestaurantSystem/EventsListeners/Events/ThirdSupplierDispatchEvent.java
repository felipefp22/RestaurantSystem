package com.RestaurantSystem.EventsListeners.Events;

import com.RestaurantSystem.Entities.Order.Order;

public class ThirdSupplierDispatchEvent {
    private final Order order;

    // <>------------ Constructors ------------<>
    public ThirdSupplierDispatchEvent(Order order) {
        this.order = order;
    }


    // <>------------ Getters ------------<>

    public Order getOrder() {
        return order;
    }
}
