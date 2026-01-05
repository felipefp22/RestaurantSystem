package com.RestaurantSystem.Services.Utils;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Customer.Customer;

public class DeliveryFeeAndDistance {
    public static int calculateEstimatedKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in km

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Haversine distance in km

        // Apply correction factor
        distance *= 1.4;

        // Round up to the next integer
        return (int) Math.ceil(distance);
    }

    public static Double calculateDeliveryRawFee(Company company, int deliveryDistanceKM) {
        Double priceToSet = company.getBaseDeliveryTax();
        Integer extraKm = deliveryDistanceKM > company.getBaseDeliveryDistanceKM() ? (int) Math.ceil(deliveryDistanceKM - company.getBaseDeliveryDistanceKM()) : 0;

        priceToSet += (extraKm * company.getTaxPerExtraKM());

        return priceToSet;
    }

    public static Double customerDeliveryFeeRaw(Company company, Customer customer) {
        int deliveryDistanceKM = customer.getDistanceFromStoreKM() != null ? customer.getDistanceFromStoreKM() : calculateEstimatedKm(company.getCompanyLat(), company.getCompanyLng(), customer.getLat(), customer.getLng());
        Double rawDeliveryFee = calculateDeliveryRawFee(company, deliveryDistanceKM);

        return (customer.getExtraDeliveryFee() == null) ? rawDeliveryFee : rawDeliveryFee + customer.getExtraDeliveryFee();
    }

    public static Double customerDeliveryFeePlusExtraFee(Company company, Customer customer) {
        int deliveryDistanceKM = customer.getDistanceFromStoreKM() != null ? customer.getDistanceFromStoreKM() : calculateEstimatedKm(company.getCompanyLat(), company.getCompanyLng(), customer.getLat(), customer.getLng());
        Double rawDeliveryFee = calculateDeliveryRawFee(company, deliveryDistanceKM);

        return (customer.getExtraDeliveryFee() == null) ? rawDeliveryFee : rawDeliveryFee + customer.getExtraDeliveryFee();
    }

}
