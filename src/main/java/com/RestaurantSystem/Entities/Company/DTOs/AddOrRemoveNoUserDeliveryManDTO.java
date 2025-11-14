package com.RestaurantSystem.Entities.Company.DTOs;

import java.util.UUID;

public record AddOrRemoveNoUserDeliveryManDTO (
        UUID companyID,
        String noUserDeliveryMan
){
}
