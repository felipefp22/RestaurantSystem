package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.Customer.DTOs.CreateOrUpdateCustomerDTO;
import com.RestaurantSystem.Entities.Customer.DTOs.FindCustomerDTO;
import com.RestaurantSystem.Infra.auth.RetriveAuthInfosService;
import com.RestaurantSystem.Services.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    private final CustomerService customerService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    public CustomerController(CustomerService customerService, RetriveAuthInfosService retriveAuthInfosService) {
        this.customerService = customerService;
        this.retriveAuthInfosService = retriveAuthInfosService;
    }

    // <> ---------- Methods ---------- <>
    @GetMapping("/get-all-customers/{companyID}")
    public ResponseEntity<List<Customer>> getAllCustomers(@RequestHeader("Authorization") String authorizationHeader,
                                                          @PathVariable String companyID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = customerService.getAllCustomers(requesterID, companyID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-customer")
    public ResponseEntity<Customer> createCustomer(@RequestHeader("Authorization") String authorizationHeader,
                                                   @RequestBody CreateOrUpdateCustomerDTO customerToCreate) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = customerService.createCustomer(requesterID, customerToCreate);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-customer")
    public ResponseEntity<Customer> updateCustomer(@RequestHeader("Authorization") String authorizationHeader,
                                                   @RequestBody CreateOrUpdateCustomerDTO customerToUpdate) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = customerService.updateCustomer(requesterID, customerToUpdate);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-customer/{customerId}")
    public ResponseEntity<String> deleteCustomer(@RequestHeader("Authorization") String authorizationHeader,
                                                   @RequestBody FindCustomerDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        customerService.deleteCustomer(requesterID, dto);

        return ResponseEntity.noContent().build();
    }

}
