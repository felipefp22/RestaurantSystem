package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsIFoodDTO(
        List<Benefits> benefits,
        String orderType,
        Payments payments,
        Merchant merchant,
        String salesChannel,
        String category,
        Picking picking,
        String orderTiming,
        LocalDateTime createdAt,
        Total total,
        LocalDateTime preparationStartDateTime,
        String id,
        String displayId,
        List<ItemsIfood> items,
        Customer customer,
        String extraInfo,
        List<AdditionalFees> additionalFees,
        Delivery delivery,
        Schedule schedule,
        Indoor indoor,
        DineIn dineIn,
        Takeout takeout,
        Error error
) {

    // <>--------------- Benefits ---------------<>
    public record Benefits(
            String targetId,
            List<SponsorshipValues> sponsorshipValues,
            Double value,
            String target,
            Campaign campaign
    ) {
        public record SponsorshipValues(
                String name,
                Double value,
                String description
        ) {
        }

        public record Campaign(
                String id,
                String name
        ) {
        }
    }

    // <>--------------- Payments ---------------<>
    public record Payments(
            List<Methods> methods,
            Double pending,
            Double prepaid
    ) {
        public record Methods(
                Wallet wallet,
                String method,
                Boolean prepaid,
                String currency,
                String type,
                Double value,
                Cash cash,
                Card card,
                Transaction transaction

        ) {

            public record Wallet(
                    String name
            ) {
            }

            public record Cash(
                    Double changeFor
            ) {
            }

            public record Card(
                    String brand
            ) {
            }

            public record Transaction(
                    String authorizationCode,
                    String acquirerDocument
            ) {
            }
        }
    }

    public record Merchant(
            String id,
            String name
    ) {
    }

    public record Picking(
            String picker,
            String replacementOptions
    ) {
    }

    public record Total(
            Double benefits,
            Double deliveryFee,
            Double orderAmount,
            Double subTotal,
            Double additionalFees
    ) {
    }

    public record ItemsIfood(
            Double unitPrice,
            Integer quantity,
            String externalCode,
            Double totalPrice,
            Double index,
            String unit,
            String ean,
            Double price,
            ScalePrices scalePrices,
            String observations,
            String imageUrl,
            String name,
            String type,
            List<Options> options,
            String id,
            Double optionsPrice
    ) {

        public record ScalePrices(
                Double defaultPrice,
                List<Scales> scales
        ) {
            public record Scales(
                    Double minQuantity,
                    Double price
            ) {
            }
        }

        public record Options(
                Double unitPrice,
                String unit,
                String ean,
                Integer quantity,
                String externalCode,
                Double price,
                String name,
                String groupName,
                String type,
                Double index,
                String id,
                Double addition,
                List<Customization> customization
        ) {
            public record Customization(
                    String id,
                    String name,
                    String groupName,
                    String externalCode,
                    String type,
                    Double quantity,
                    Double unitPrice,
                    Double addition,
                    Double price
            ) {
            }
        }
    }

    public record Customer(
            Phone phone,
            String documentNumber,
            String name,
            Double ordersCountOnMerchant,
            String segmentation,
            String id
    ) {
        public record Phone(
                String number,
                String localizer,
                String localizerExpiration
        ) {
        }
    }

    public record AdditionalFees(
            String type,
            Double value,
            String description,
            String fullDescription,
            List<Liabilities> liabilities
    ) {
        public record Liabilities(
                String name,
                Double percentage
        ) {
        }
    }

    public record Delivery(
            String mode,
            String pickupCode,
            String description,
            String deliveredBy,
            DeliveryAddress deliveryAddress,
            LocalDateTime deliveryDateTime
    ) {
        public record DeliveryAddress(
                String reference,
                String country,
                String streetName,
                String formattedAddress,
                String streetNumber,
                String city,
                String postalCode,   // <-- missing in your class
                Coordinates coordinates,
                String neighborhood,
                String state,
                String complement
        ) {
            public record Coordinates(
                    Double latitude,
                    Double longitude
            ) {}
        }
    }

    public record Schedule(
            LocalDateTime deliveryDateTimeStart,
            LocalDateTime deliveryDateTimeEnd
    ) {
    }

    public record Indoor(
            String mode,
            LocalDateTime deliveryDateTime,
            String table
    ) {
    }

    public record DineIn(
            LocalDateTime deliveryDateTime
    ) {
    }

    public record Takeout(
            String mode,
            LocalDateTime takeoutDateTime
    ) {
    }

    public record Error(
            String code,
            String field,
            List<Object> details,
            String message
    ) {
    }
}
