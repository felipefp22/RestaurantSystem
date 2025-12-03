package com.RestaurantSystem.Services.ThirdSuppliersService;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CompanyThirdSuppliersToPoolingDTO;
import com.RestaurantSystem.Repositories.ShiftRepo;
import com.RestaurantSystem.Services.AuxsServices.AzureServiceBusService;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Object getOpenShiftsToPooling() {
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

            return itemsToPooling;
        } else {
            return null;
        }
    }
}
