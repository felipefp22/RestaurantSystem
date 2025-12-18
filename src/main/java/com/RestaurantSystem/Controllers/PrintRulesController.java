package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Printer.DTOs.UpdatePrintRulesDTO;
import com.RestaurantSystem.Entities.Printer.PrintRules;
import com.RestaurantSystem.Repositories.PrintRulesRepo;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.PrintRulesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/print-rules")
public class PrintRulesController {

    private final PrintRulesService printRulesService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    public PrintRulesController (PrintRulesService printRulesService, RetriveAuthInfosService retriveAuthInfosService) {
        this.printRulesService = printRulesService;
        this.retriveAuthInfosService = retriveAuthInfosService;
    }

    // <> ---------- Methods ---------- <>

    @GetMapping("/print-rules")
    public ResponseEntity<List<PrintRules>> getPrintRules(@PathVariable UUID companyID,
                                                          @RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = printRulesService.getPrintRules(companyID, requesterID);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-print-rules")
    public ResponseEntity<PrintRules> updatePrintRules(@RequestBody UpdatePrintRulesDTO dto,
                                                       @RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = printRulesService.updatePrintRules(dto, requesterID);

        return ResponseEntity.ok(response);
    }
}
