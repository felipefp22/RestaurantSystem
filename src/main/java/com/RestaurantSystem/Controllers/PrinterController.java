package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Printer.DTOs.DeletePrinterDTO;
import com.RestaurantSystem.Entities.Printer.DTOs.CreateOrUpdatePrinterDTO;
import com.RestaurantSystem.Entities.Printer.Printer;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.PrinterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/printer")
public class PrinterController {

    private final PrinterService printerService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    public PrinterController(PrinterService printerService, RetriveAuthInfosService retriveAuthInfosService) {
        this.printerService = printerService;
        this.retriveAuthInfosService = retriveAuthInfosService;
    }

    // <> ---------- Methods ---------- <>

    @GetMapping("/get-prnters/{comapnyID}")
    public ResponseEntity<List<Printer>> getPrinters(@PathVariable UUID companyID,
                                                     @RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = printerService.getPrinters(companyID, requesterID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-printer")
    public ResponseEntity<Printer> addPrinter(@RequestBody CreateOrUpdatePrinterDTO dto,
                                              @RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = printerService.addPrinter(dto, requesterID);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-printer")
    public ResponseEntity<Printer> updatePrinter(@RequestBody CreateOrUpdatePrinterDTO dto,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = printerService.updatePrinter(dto, requesterID);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-printer")
    public ResponseEntity<Void> deletePrinter(@PathVariable DeletePrinterDTO dto,
                                              @RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        printerService.deletePrinter(dto, requesterID);

        return ResponseEntity.noContent().build();
    }
}
