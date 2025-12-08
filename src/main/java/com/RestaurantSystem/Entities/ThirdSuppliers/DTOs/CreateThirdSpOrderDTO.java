package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs;

import com.RestaurantSystem.Entities.Company.DTOs.CompanyThirdSuppliersToPoolingDTO;
import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.OrderItemDTO;
import com.RestaurantSystem.Entities.Order.ThirdSuppliersEnum;
import com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs.OrderDetailsIFoodDTO;

import java.util.List;

public record CreateThirdSpOrderDTO(
        CompanyThirdSuppliersToPoolingDTO companyData,
        ThirdSuppliersEnum isThirdSpOrder,
        String tableNumberOrDeliveryOrPickup,
        String notes,
        Boolean isUserEditBlocked,
        String thirdSpOrderID,
        String customerName,
        Double lat,
        Double lng,
        String zipCode,
        String address,
        String addressNumber,
        String complementAddress,
        List<OrderItemDTO> orderItemsIDs,
        Double price,
        double discount,
        Double totalPrice,
        Double deliveryFee
) {
//
//    public CreateThirdSpOrderDTO(CompanyThirdSuppliersToPoolingDTO dto, OrderDetailsIFoodDTO ifoodOrderDetails, String tableNumberOrDeliveryOrPickup){
//        this(
//                dto,
//                ThirdSuppliersEnum.IFOOD,
//                tableNumberOrDeliveryOrPickup,
//                ifoodOrderDetails.notes(),
//                false,
//                ifoodOrderDetails.id(),
//                ifoodOrderDetails.customerName(),
//                ifoodOrderDetails.lat(),
//                ifoodOrderDetails.lng(),
//                ifoodOrderDetails.zipCode(),
//                ifoodOrderDetails.address(),
//                ifoodOrderDetails.addressNumber(),
//                ifoodOrderDetails.complementAddress(),
//                ifoodOrderDetails.orderItemsIDs(),
//                ifoodOrderDetails.price(),
//                ifoodOrderDetails.discount(),
//                ifoodOrderDetails.totalPrice(),
//                ifoodOrderDetails.deliveryFee()
//        );
//    }
}
