package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.Customer.DTOs.CreateOrUpdateCustomerDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.CustomerRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final CustomerRepo customerRepo;

    public CustomerService(AuthUserRepository authUserRepository, CompanyRepo companyRepo, CustomerRepo customerRepo) {
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.customerRepo = customerRepo;
    }

    // <> ---------- Methods ---------- <>
    public List<Customer> getAllCustomers(String requesterID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return company.getCustomers();
    }

    public Customer createCustomer(String requesterID, CreateOrUpdateCustomerDTO customerToCreateDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Customer newCustomer = new Customer(company, customerToCreateDTO);

        customerRepo.save(newCustomer);

        return newCustomer;
    }

    public Customer updateCustomer(String requesterID, CreateOrUpdateCustomerDTO customerToUpdateDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Customer existingCustomer = customerRepo.findById(customerToUpdateDTO.id())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        existingCustomer.setCustomerName(customerToUpdateDTO.customerName());
        existingCustomer.setPhone(customerToUpdateDTO.phone());
        existingCustomer.setAddress(customerToUpdateDTO.address());
        existingCustomer.setAddressNumber(customerToUpdateDTO.addressNumber());
        existingCustomer.setCity(customerToUpdateDTO.city());
        existingCustomer.setState(customerToUpdateDTO.state());
        existingCustomer.setZipCode(customerToUpdateDTO.zipCode());
        existingCustomer.setLat(customerToUpdateDTO.lat());
        existingCustomer.setLng(customerToUpdateDTO.lng());
        existingCustomer.setComplement(customerToUpdateDTO.complement());

        customerRepo.save(existingCustomer);

        return existingCustomer;
    }

    public void deleteCustomer(String requesterID, UUID customerId) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Customer existingCustomer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwnerCompound().equals(requesterID))
            throw new RuntimeException("You are not allowed to del a customer, ask to manager");

        customerRepo.deleteById(existingCustomer.getId());
    }
}
