package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.ShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    private final ShiftService shiftService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    public ShiftController(ShiftService shiftService, RetriveAuthInfosService retriveAuthInfosService) {
        this.shiftService = shiftService;
        this.retriveAuthInfosService = retriveAuthInfosService;
    }

    //<>------------ Methods ------------<>
    @GetMapping("/get-all-shifts/{companyID}")
    public ResponseEntity<List<Shift>> getAllShifts(@RequestHeader("Authorization") String authorizationHeader,
                                                    @PathVariable String companyID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = shiftService.getAllShifts(requesterID, companyID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-shift/{companyID}")
    private ResponseEntity<Shift> createShift(@RequestHeader("Authorization") String authorizationHeader,
                                              @PathVariable String companyID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = shiftService.createShift(requesterID, companyID);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/close-shift/{companyID}")
    private ResponseEntity<Shift> closeShift(@RequestHeader("Authorization") String authorizationHeader,
                                             @PathVariable String companyID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = shiftService.closeShift(requesterID, companyID);

        return ResponseEntity.ok(response);
    }
}
