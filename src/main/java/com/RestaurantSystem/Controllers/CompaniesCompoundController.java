package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CreateOrUpdateCompoundDTO;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.CompaniesCompoundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/companies-compound")
public class CompaniesCompoundController {

    private final RetriveAuthInfosService retriveAuthInfosService;
    private final CompaniesCompoundService companiesCompoundService;

    public CompaniesCompoundController(RetriveAuthInfosService retriveAuthInfosService, CompaniesCompoundService companiesCompoundService) {
        this.retriveAuthInfosService = retriveAuthInfosService;
        this.companiesCompoundService = companiesCompoundService;
    }

    // <> ------------- Methods ------------- <>


    @PostMapping("/create-compound")
    public ResponseEntity<CompaniesCompound> createCompaniesCompound(@RequestHeader("Authorization") String authorizationHeader,
                                                                    @RequestBody CreateOrUpdateCompoundDTO createCompoundDTO) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        CompaniesCompound companiesCompoundCreated = companiesCompoundService.createCompaniesCompound(requesterID, createCompoundDTO);

        return ResponseEntity.ok(companiesCompoundCreated);
    }

    @PutMapping("/update-compound")
    public ResponseEntity<CompaniesCompound> updateCompaniesCompound(@RequestHeader("Authorization") String authorizationHeader,
                                                                    @RequestBody CreateOrUpdateCompoundDTO updateCompoundDTO) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        CompaniesCompound companiesCompoundUpdated = companiesCompoundService.updateCompaniesCompound(requesterID, updateCompoundDTO);

        return ResponseEntity.ok(companiesCompoundUpdated);
    }
}
