package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Printer.DTOs.DeletePrinterDTO;
import com.RestaurantSystem.Entities.Printer.DTOs.CreateOrUpdatePrinterDTO;
import com.RestaurantSystem.Entities.Printer.Printer;
import com.RestaurantSystem.Entities.Printer.PrintersAndCopies;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.PrinterRepo;
import com.RestaurantSystem.Repositories.PrintersAndCopiesRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PrinterService {
    private final PrinterRepo printerRepo;
    private final VerificationsServices verificationsServices;
    private final PrintersAndCopiesRepo printersAndCopiesRepo;

    public PrinterService(PrinterRepo printerRepo, VerificationsServices verificationsServices, PrintersAndCopiesRepo printersAndCopiesRepo) {
        this.printerRepo = printerRepo;
        this.verificationsServices = verificationsServices;
        this.printersAndCopiesRepo = printersAndCopiesRepo;
    }


    // <> ---------- Methods ---------- <>

    public Set<Printer> getPrinters(UUID companyID, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);

        return company.getPrinters();
    }

    public Printer addPrinter(CreateOrUpdatePrinterDTO dto, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManager(company, requester);

        newPrinterValidations(dto, company);
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

        if ((company.getPrinters().stream().anyMatch(x -> !x.getId().equals(printerToUpdate.getId()) && x.getPrinterCustomName().equalsIgnoreCase(dto.printerCustomName()))))
            throw new RuntimeException("A printer with the same custom name already exists in the company");

        if (printerToUpdate.getType().equalsIgnoreCase("NETWORK") && printerToUpdate.getMac().equalsIgnoreCase(dto.mac())) {
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

        Printer printerToDelete = company.getPrinters().stream()
                .filter(prn -> prn.getId().equals(dto.printerID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Printer not found"));

        company.getPrintRules().forEach(prr -> {
            List<PrintersAndCopies> pAndCopiesToDelete = prr.getPrintersAndCopies().stream().filter(pac -> pac.getPrinterID().equals(printerToDelete.getId())).toList();
            prr.getPrintersAndCopies().removeAll(pAndCopiesToDelete);
            printersAndCopiesRepo.deleteAll(pAndCopiesToDelete);
        });

        company.getPrinters().remove(printerToDelete);
        printerRepo.delete(printerToDelete);
    }

    // <> ---------- Helpers ---------- <>
    private void newPrinterValidations(CreateOrUpdatePrinterDTO dto, Company company) {
        if (dto.printerCustomName() == null || dto.printerCustomName().isBlank())
            throw new RuntimeException("Printer custom name cannot be null or blank");
        if (dto.type() == null || dto.type().isBlank())
            throw new RuntimeException("Printer type cannot be null or blank");
        if (dto.mac() == null && dto.ip() == null && dto.usbName() == null)
            throw new RuntimeException("At least one of mac, ip or usbName must be provided");

        if (company.getPrinters().stream().anyMatch(x -> x.getMac().equalsIgnoreCase(dto.mac())))
            throw new RuntimeException("A printer with the same MAC address already exists in the company");
        if (company.getPrinters().stream().anyMatch(x -> x.getUsbName() != null && x.getUsbName().equalsIgnoreCase(dto.usbName())))
            throw new RuntimeException("A printer with the same USB name already exists in the company");
        if (company.getPrinters().stream().anyMatch(x -> x.getIp() != null && x.getIp().equalsIgnoreCase(dto.ip())))
            throw new RuntimeException("A printer with the same IP address already exists in the company");
        if ((company.getPrinters().stream().anyMatch(x -> x.getPrinterCustomName().equalsIgnoreCase(dto.printerCustomName()))))
            throw new RuntimeException("A printer with the same custom name already exists in the company");
    }
}
