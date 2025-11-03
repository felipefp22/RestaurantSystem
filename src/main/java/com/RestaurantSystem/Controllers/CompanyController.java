package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.Company.DTOs.*;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/get-company-operation/{companyID}")
    public ResponseEntity<CompanyOperationDTO> getCompanyOperation(@RequestHeader("Authorization") String authorizationHeader,
                                                                   @PathVariable String companyID) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = companyService.getCompanyOperation(requesterID, companyID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-company")
    public ResponseEntity<Company> createCompany(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestBody CreateCompanyDTO createCompanyDTO) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = companyService.createCompany(requesterID, createCompanyDTO);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-company")
    public ResponseEntity updateCompany(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestBody UpdateCompanyDTO updateCompanyDTO) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = companyService.updateCompany(requesterID, updateCompanyDTO);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/set-company-geo-location")
    public ResponseEntity<Company> setCompanyGeoLocation(@RequestHeader("Authorization") String authorizationHeader,
                                                         @RequestBody UpdateCompanyDTO updateCompanyDTO) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = companyService.setCompanyGeoLocation(requesterID, updateCompanyDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-employees/{companyID}")
    public ResponseEntity<List<CompanyEmployeesDTO>> getEmployees(@RequestHeader("Authorization") String authorizationHeader,
                                                                  @PathVariable String companyID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = companyService.getEmployees(requesterID, companyID);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/add-employees")
    public ResponseEntity<List<CompanyEmployees>> addEmployeeToCompany(@RequestHeader("Authorization") String authorizationHeader,
                                                                       @RequestBody AddOrUpdateEmployeeDTO employeeDTO) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = companyService.addEmployeeToCompany(requesterID, employeeDTO);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-employee-position")
    public ResponseEntity<List<CompanyEmployees>> updateEmployeePosition(@RequestHeader("Authorization") String authorizationHeader,
                                                                         @RequestBody AddOrUpdateEmployeeDTO employeeDTO) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = companyService.updateEmployeePosition(requesterID, employeeDTO);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/remove-employee")
    public ResponseEntity<List<CompanyEmployees>> removeEmployeeFromCompany(@RequestHeader("Authorization") String authorizationHeader,
                                                                            @RequestBody AddOrUpdateEmployeeDTO employeeDTO) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = companyService.removeEmployeeFromCompany(requesterID, employeeDTO);

        return ResponseEntity.ok(response);
    }
}
