package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Printer.DTOs.UpdatePrintRulesDTO;
import com.RestaurantSystem.Entities.Printer.PrintRules;
import com.RestaurantSystem.Entities.Printer.PrintersAndCopies;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.PrintRulesRepo;
import com.RestaurantSystem.Repositories.PrintersAndCopiesRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PrintRulesService {

    private final PrintRulesRepo printRulesRepo;
    private final VerificationsServices verificationsServices;
    private final PrintersAndCopiesRepo printersAndCopiesRepo;

    public PrintRulesService(PrintRulesRepo printRulesRepo, VerificationsServices verificationsServices, PrintersAndCopiesRepo printersAndCopiesRepo) {
        this.printRulesRepo = printRulesRepo;
        this.verificationsServices = verificationsServices;
        this.printersAndCopiesRepo = printersAndCopiesRepo;
    }

    // <> ---------- Methods ---------- <>
    public List<PrintRules> getPrintRules(UUID companyID, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);

        return company.getPrintRules();
    }

    public List<PrintRules> updatePrintRules(UpdatePrintRulesDTO dto, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManager(company, requester);

        PrintRules printRulesToUpdate = company.getPrintRules().stream()
                .filter(prr -> prr.getPrintCategory().equals(dto.printCategory()))
                .findFirst()
                .orElse(new PrintRules(company, dto.printCategory()));

        if (dto.printAndCopiesID() == null && dto.printerID() != null && dto.copies() != null) {
            PrintersAndCopies pAndCopiesFound = printRulesToUpdate.getPrintersAndCopies().stream()
                    .filter(pc -> pc.getPrinterID().equals(dto.printerID()))
                    .findFirst()
                    .orElse(new PrintersAndCopies(printRulesToUpdate, dto.printerID(), dto.copies()));
            pAndCopiesFound.setCopies(dto.copies());
            printRulesToUpdate.getPrintersAndCopies().add(pAndCopiesFound);
            printersAndCopiesRepo.save(pAndCopiesFound);

        } else if (dto.printAndCopiesID() != null) {
            PrintersAndCopies pAndCopiesToUpdate = printRulesToUpdate.getPrintersAndCopies().stream()
                    .filter(pc -> pc.getId().equals(dto.printAndCopiesID()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("PrintersAndCopies with ID " + dto.printAndCopiesID() + " not found."));
            if(dto.printerID() == null) {
                printRulesToUpdate.getPrintersAndCopies().remove(pAndCopiesToUpdate);
                printersAndCopiesRepo.delete(pAndCopiesToUpdate);
            } else {
                pAndCopiesToUpdate.setPrinterID(dto.printerID());
                pAndCopiesToUpdate.setCopies(dto.copies());
            }
        }

        printRulesRepo.save(printRulesToUpdate);
        return company.getPrintRules();
    }

}
