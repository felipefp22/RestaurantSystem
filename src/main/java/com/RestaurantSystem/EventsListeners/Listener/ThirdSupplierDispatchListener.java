package com.RestaurantSystem.EventsListeners.Listener;

import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Order.ThirdSuppliersEnum;
import com.RestaurantSystem.EventsListeners.Events.ThirdSupplierDispatchEvent;
import com.RestaurantSystem.Services.ThirdSuppliersService.IFoodService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ThirdSupplierDispatchListener {
    private final IFoodService ifoodService;

    public ThirdSupplierDispatchListener(IFoodService ifoodService) {
        this.ifoodService = ifoodService;
    }


    // <>------------ Methods ------------<>
    @EventListener
    public void handleDispatch(ThirdSupplierDispatchEvent event) {
        Order order = event.getOrder();
        if (order.getIsThirdSpOrder() == null) return;

        if (order.getIsThirdSpOrder().equals(ThirdSuppliersEnum.IFOOD))
            ifoodService.dispatchIFood(order.getShift().getCompany().getCompanyIFoodData(), order.getThirdSpOrderID());
    }

}
