package com.RestaurantSystem.Entities.Customer.DTOs;

import com.RestaurantSystem.Entities.Customer.Customer;

import java.util.UUID;

public record CustomerResumeDTO(
        UUID customerID,
        UUID companyID,
        String customerName,
        String phone,
        String address,
        String addressNumber,
        String city,
        String state,
        String zipCode,
        Double lat,
        Double lng,
        String complement

) {
    public CustomerResumeDTO (Customer customer) {
        this(
                customer.getId(),
                customer.getCompany().getId(),
                customer.getCustomerName(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getAddressNumber(),
                customer.getCity(),
                customer.getState(),
                customer.getZipCode(),
                customer.getLat(),
                customer.getLng(),
                customer.getComplement()
        );
    }
}
