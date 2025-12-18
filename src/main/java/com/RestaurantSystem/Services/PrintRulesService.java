package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Printer.DTOs.UpdatePrintRulesDTO;
import com.RestaurantSystem.Entities.Printer.PrintRules;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.PrintRulesRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PrintRulesService {

    private final PrintRulesRepo printRulesRepo;
    private final VerificationsServices verificationsServices;

    public PrintRulesService(PrintRulesRepo printRulesRepo, VerificationsServices verificationsServices) {
        this.printRulesRepo = printRulesRepo;
        this.verificationsServices = verificationsServices;
    }

    // <> ---------- Methods ---------- <>
    public List<PrintRules> getPrintRules(UUID companyID, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);

        return company.getPrintRules();
    }

    public PrintRules updatePrintRules(UpdatePrintRulesDTO dto, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManager(company, requester);

        PrintRules printRulesToUpdate = company.getPrintRules().stream()
                .filter(prr -> prr.getPrintCategory().equals(dto.printCategory()))
                .findFirst()
                .orElse(new PrintRules(company, dto.printCategory()));

        printRulesToUpdate.setPrinterID(dto.printerID());
        printRulesToUpdate.setCopies(dto.copies());

        return printRulesRepo.save(printRulesToUpdate);
    }

}
