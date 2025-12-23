package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Printer.DTOs.DeletePrintSyncsDTO;
import com.RestaurantSystem.Services.AuxsServices.PrintSyncService;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/print-sync")
public class PrintSyncController {

    private final RetriveAuthInfosService retriveAuthInfosService;
    private final PrintSyncService printSyncService;

    public PrintSyncController(RetriveAuthInfosService retriveAuthInfosService, PrintSyncService printSyncService) {
        this.retriveAuthInfosService = retriveAuthInfosService;
        this.printSyncService = printSyncService;
    }

    // <> ---------- Methods ---------- <>

    @PutMapping("/delete-print-syncs")
    private ResponseEntity deletePrintSync(@RequestBody DeletePrintSyncsDTO dto,
                                           @RequestHeader("Authorization") String authorizationHeader) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        printSyncService.deletePrintSyncs(dto, requesterID);

        return ResponseEntity.ok().build();
    }
}
