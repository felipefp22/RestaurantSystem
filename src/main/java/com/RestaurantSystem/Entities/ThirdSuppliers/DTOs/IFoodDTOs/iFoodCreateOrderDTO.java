package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.OrderItemDTO;

import java.time.LocalDateTime;
import java.util.List;

public record iFoodCreateOrderDTO(
        Company company,
        String orderNumber,
        String pickupOrDelivery,
        LocalDateTime createdAtUTC,
        LocalDateTime scheduledForUTC,
        Double prePaid,
        Double pendingPaymentPart,
        Double orderAmount,
        Double deliveryFee,
        Double additionalFees,
        String phoneIfood,
        String localizer,
        String customerName,
        String documentNumber,
        String pickUpCode,
        String street,
        String city,
        String spotNumber,
        String postalCode,
        Double lat,
        Double lng,
        List<OrderItemDTO> orderItemsDTOs
) {
    public iFoodCreateOrderDTO(Company company, OrderDetailsIFoodDTO ifoodOrderDetails, List<OrderItemDTO> orderItemsDTOs) {
        this(
                company,
                ifoodOrderDetails.displayId(),
                getPickupOrDelivery(ifoodOrderDetails),
                ifoodOrderDetails.createdAt(),
                getScheduledFor(ifoodOrderDetails),
                ifoodOrderDetails.payments().prepaid(),
                ifoodOrderDetails.payments().pending(),
                ifoodOrderDetails.total().orderAmount(),
                ifoodOrderDetails.total().deliveryFee(),
                ifoodOrderDetails.total().additionalFees(),
                ifoodOrderDetails.customer().phone().number(),
                ifoodOrderDetails.customer().phone().localizer(),
                ifoodOrderDetails.customer().name(),
                ifoodOrderDetails.customer().documentNumber(),
                ifoodOrderDetails.delivery().pickupCode(),
                ifoodOrderDetails.delivery().deliveryAddress().streetName(),
                ifoodOrderDetails.delivery().deliveryAddress().city(),
                ifoodOrderDetails.delivery().deliveryAddress().streetNumber(),
                ifoodOrderDetails.delivery().deliveryAddress().postalCode(),
                ifoodOrderDetails.delivery().deliveryAddress().coordinates().latitude(),
                ifoodOrderDetails.delivery().deliveryAddress().coordinates().longitude(),
                orderItemsDTOs
        );
    }

    // <>--------------- Helpers ---------------<>

    private static String getPickupOrDelivery(OrderDetailsIFoodDTO ifoodOrderDetails) {
        switch (ifoodOrderDetails.orderType()) {
            case "TAKEOUT":
                return "pickuo";
            case "DELIVERY":
                return "delivery";
            default:
                throw new IllegalArgumentException("Unknown order type: " + ifoodOrderDetails.orderType());
        }
    }

    private static LocalDateTime getScheduledFor(OrderDetailsIFoodDTO ifoodOrderDetails) {
        if (ifoodOrderDetails.orderTiming().equals("SCHEDULED")) {

            return ifoodOrderDetails.schedule().deliveryDateTimeStart();
        } else {
            return null;
        }
    }
}
