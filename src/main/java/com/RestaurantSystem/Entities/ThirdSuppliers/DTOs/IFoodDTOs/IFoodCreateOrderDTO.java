package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.OrderItemDTO;

import java.time.LocalDateTime;
import java.util.List;

public record IFoodCreateOrderDTO(
        Company company,
        String ifoodOrderID,
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
        String addressComplement,
        String addressReference,
        Double lat,
        Double lng,
        List<OrderItemDTO> orderItemsDTOs
) {
    public IFoodCreateOrderDTO(Company company, OrderDetailsIFoodDTO ifoodOrderDetails, List<OrderItemDTO> orderItemsDTOs) {
        this(
                company,
                ifoodOrderDetails.id(),
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
                ifoodOrderDetails.delivery() != null ? ifoodOrderDetails.delivery().pickupCode() : null,
                ifoodOrderDetails.delivery() != null ? ifoodOrderDetails.delivery().deliveryAddress().streetName() : null,
                ifoodOrderDetails.delivery() != null ? ifoodOrderDetails.delivery().deliveryAddress().city() : null,
                ifoodOrderDetails.delivery() != null ? ifoodOrderDetails.delivery().deliveryAddress().streetNumber() : null,
                ifoodOrderDetails.delivery() != null ? ifoodOrderDetails.delivery().deliveryAddress().postalCode() : null,
                ifoodOrderDetails.delivery() != null ? ifoodOrderDetails.delivery().deliveryAddress().complement() : null,
                ifoodOrderDetails.delivery() != null ? ifoodOrderDetails.delivery().deliveryAddress().reference() : null,
                ifoodOrderDetails.delivery() != null ? ifoodOrderDetails.delivery().deliveryAddress().coordinates().latitude() : null,
                ifoodOrderDetails.delivery() != null ? ifoodOrderDetails.delivery().deliveryAddress().coordinates().longitude() : null,
                orderItemsDTOs
        );
    }

    // <>--------------- Helpers ---------------<>

    private static String getPickupOrDelivery(OrderDetailsIFoodDTO ifoodOrderDetails) {
        switch (ifoodOrderDetails.orderType()) {
            case "TAKEOUT":
                return "pickup";
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
