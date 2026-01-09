package com.RestaurantSystem.EventsListeners.Listener;

import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Order.ThirdSuppliersEnum;
import com.RestaurantSystem.EventsListeners.Events.ThirdSupplierDeliveredEvent;
import com.RestaurantSystem.EventsListeners.Events.ThirdSupplierDispatchEvent;
import com.RestaurantSystem.EventsListeners.Events.ThirdSupplierReadyToPickupEvent;
import com.RestaurantSystem.Services.ThirdSuppliersService.IFoodService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ThirdSupplierReadyToPickupListener {
    private final IFoodService ifoodService;

    public ThirdSupplierReadyToPickupListener(IFoodService ifoodService) {
        this.ifoodService = ifoodService;
    }


    // <>------------ Methods ------------<>
    @EventListener
    public void handleReadyToPickupIFood(ThirdSupplierReadyToPickupEvent event) {
        Order order = event.getOrder();
        if (order.getIsThirdSpOrder() == null) return;

        if (order.getIsThirdSpOrder().equals(ThirdSuppliersEnum.IFOOD))
            ifoodService.readyToPickupIFood(order.getShift().getCompany().getCompanyIFoodData(), order.getThirdSpOrderID());
    }

}
