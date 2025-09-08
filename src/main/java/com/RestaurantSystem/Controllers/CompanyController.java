package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Infra.auth.RetriveAuthInfosService;
import com.RestaurantSystem.Services.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    public CompanyController(CompanyService companyService, RetriveAuthInfosService retriveAuthInfosService) {
        this.companyService = companyService;
        this.retriveAuthInfosService = retriveAuthInfosService;
    }


    // <> ------------- Methods ------------- <>

    @PostMapping("/create-company")
    public ResponseEntity<Company> createCompany(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestBody CreateCompanyDTO createCompanyDTO) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = companyService.createCompany(requesterID, createCompanyDTO);

        return ResponseEntity.ok(response);
    }
}
