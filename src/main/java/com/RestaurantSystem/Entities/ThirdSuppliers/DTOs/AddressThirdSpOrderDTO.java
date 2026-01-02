//package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs;
//
//public record AddressThirdSpOrderDTO(
//        Boolean isThirdSpAddr,
//        String customerName,
//        Double lat,
//        Double lng,
//        String zipCode,
//        String address,
//        String addressNumber,
//        String complementAddress
//        ) {
//
//    public AddressThirdSpOrderDTO(CreateThirdSpOrderDTO thirdSpDTO) {
//        this(
//            true,
//            thirdSpDTO.customerName(),
//            thirdSpDTO.lat(),
//            thirdSpDTO.lng(),
//            thirdSpDTO.zipCode(),
//            thirdSpDTO.address(),
//            thirdSpDTO.addressNumber(),
//            thirdSpDTO.complementAddress()
//        );
//    }
//}
