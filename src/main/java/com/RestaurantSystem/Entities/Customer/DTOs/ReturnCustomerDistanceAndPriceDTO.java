package com.RestaurantSystem.Entities.Customer.DTOs;

public record ReturnCustomerDistanceAndPriceDTO(
        Integer km,
        Double rawDeliveryFee
) {
}
