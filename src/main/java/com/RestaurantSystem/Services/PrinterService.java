package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Printer.DTOs.DeletePrinterDTO;
import com.RestaurantSystem.Entities.Printer.DTOs.CreateOrUpdatePrinterDTO;
import com.RestaurantSystem.Entities.Printer.Printer;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.PrinterRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PrinterService {
    private final PrinterRepo printerRepo;
    private final VerificationsServices verificationsServices;

    public PrinterService(PrinterRepo printerRepo, VerificationsServices verificationsServices) {
        this.printerRepo = printerRepo;
        this.verificationsServices = verificationsServices;
    }

    public List<Printer> getPrinters(UUID companyID, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);

        return company.getPrinters();
    }

    public Printer addPrinter(CreateOrUpdatePrinterDTO dto, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManager(company, requester);

        Printer newPrinter = new Printer(dto, company);

        return printerRepo.save(newPrinter);
    }

    public Printer updatePrinter(CreateOrUpdatePrinterDTO dto, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManager(company, requester);

        Printer printerToUpdate = company.getPrinters().stream()
                .filter(prn -> prn.getId().equals(dto.printerID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Printer not found"));

        if(printerToUpdate.getType().equalsIgnoreCase("NETWORK") && printerToUpdate.getMac().equalsIgnoreCase(dto.mac())) {
            printerToUpdate.setPrinterCustomName(dto.printerCustomName());
            printerToUpdate.setIp(dto.ip());
        } else {
            printerToUpdate.setPrinterCustomName(dto.printerCustomName());
        }

        return printerRepo.save(printerToUpdate);
    }

    public void deletePrinter(DeletePrinterDTO dto, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManager(company, requester);
    }

}
