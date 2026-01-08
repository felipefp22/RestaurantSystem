package com.RestaurantSystem.Entities.Order.DTOs;

import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.CustomOrderItemsDTO;
import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.OrderItemDTO;

import java.util.List;
import java.util.UUID;

public record CreateOrderDTO(
        UUID companyID,
        String tableNumberOrDeliveryOrPickup,
        UUID customerID,
        String pickupName,
        List<OrderItemDTO> orderItemsIDs,
        String notes,
        Integer deliveryDistanceKM,
        Double discountValue,
        Double money,
        Double pix,
        Double debit,
        Double credit,
        Double valeRefeicao,
        Double othersPaymentModes
) {

}
