package com.RestaurantSystem.Services.ThirdSuppliersService;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CompanyThirdSuppliersToPoolingDTO;
import com.RestaurantSystem.Repositories.ShiftRepo;
import com.RestaurantSystem.Services.AuxsServices.AzureServiceBusService;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusSenderClientBuilderFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static org.springframework.util.SerializationUtils.serialize;

@Service
public class ServiceBusThirdSuppliersService {

    private final AzureServiceBusService azureServiceBusService;
    private final ShiftRepo shiftRepo;

    public ServiceBusThirdSuppliersService(AzureServiceBusService azureServiceBusService, ShiftRepo shiftRepo) {
        this.azureServiceBusService = azureServiceBusService;
        this.shiftRepo = shiftRepo;
    }


    // <> ------------- Methods ------------- <>
    public void sendTestMessage() {

        azureServiceBusService.sendMessageToQueue("ThirdSuppliersPooling", "<>-- Hello service BUS! --<>");
    }

    public void getOpenShiftsToPooling() throws JsonProcessingException {
        var openShifts = shiftRepo.findAllByEndTimeUTCIsNull();

        if (openShifts.isPresent()) {
            List<Company> companiesWithOpenShiftsAndHaveThirdSupplier = openShifts.get().stream()
                    .map(shift -> shift.getCompany())
                    .filter(company -> company.getCompanyIFoodData() != null)
                    .distinct()
                    .toList();

            List<CompanyThirdSuppliersToPoolingDTO> itemsToPooling = companiesWithOpenShiftsAndHaveThirdSupplier.stream()
                    .map(company -> new CompanyThirdSuppliersToPoolingDTO(company.getId(), company.getCompanyIFoodData()))
                    .toList();

            azureServiceBusService.sendMessageToPoolingThirdSuppliers(itemsToPooling);
        } else {

        }
    }

    // <>------------- Pooling -------------<>

}
