package com.RestaurantSystem.Entities.Order.DTOs;

import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.CustomOrderItemsDTO;
import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.OrderItemDTO;

import java.util.List;
import java.util.UUID;

public record ProductsToAddOnOrderDTO(
        UUID companyID,
        UUID orderID,
        List<OrderItemDTO> orderItemsIDs
        ) {


}
