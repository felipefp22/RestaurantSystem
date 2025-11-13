package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.Customer.DTOs.CreateOrUpdateCustomerDTO;
import com.RestaurantSystem.Entities.Customer.DTOs.CustomerResumeDTO;
import com.RestaurantSystem.Entities.Customer.DTOs.FindCustomerDTO;
import com.RestaurantSystem.Entities.Customer.DTOs.SearchCustomerDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.CustomerRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomerService {
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final CustomerRepo customerRepo;
    private final VerificationsServices verificationsServices;

    public CustomerService(AuthUserRepository authUserRepository, CompanyRepo companyRepo, CustomerRepo customerRepo, VerificationsServices verificationsServices) {
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.customerRepo = customerRepo;
        this.verificationsServices = verificationsServices;
    }

    // <> ---------- Methods ---------- <>
//    public List<CustomerResumeDTO> getMatchCustomers(String requesterID, SearchCustomerDTO searchDTO) {
//        AuthUserLogin requester = authUserRepository.findById(requesterID)
//                .orElseThrow(() -> new RuntimeException("Requester not found"));
//
//        Company company = companyRepo.findById(UUID.fromString(searchDTO.companyID()))
//                .orElseThrow(() -> new RuntimeException("Company not found"));
//
//        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the categories of this company");
//
//        List<Customer> allCompanyCustomers = company.getCustomers();
//
//        List<Customer> filteredCustomers = allCompanyCustomers.stream()
//                .filter(c -> c.getCustomerName().toLowerCase().contains(searchDTO.searchString().toLowerCase()) ||
//                             (c.getPhone() != null && c.getPhone().toLowerCase().contains(searchDTO.searchString().toLowerCase())))
//                .toList();
//
//        return filteredCustomers.stream()
//                .map(CustomerResumeDTO::new)
//                .toList();
//    }

    public List<Customer> getAllCustomers(String requesterID, String companyID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(companyID))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        return company.getCustomers();
    }

    public Customer createCustomer(String requesterID, CreateOrUpdateCustomerDTO customerToCreateDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(customerToCreateDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");
        if (company.getCustomers().stream().anyMatch(x -> x.getPhone().equals(customerToCreateDTO.phone())))
            throw new RuntimeException("thisPhoneAlreadyInUse");

        Customer newCustomer = new Customer(company, customerToCreateDTO);

        customerRepo.save(newCustomer);

        return newCustomer;
    }

    public Customer updateCustomer(String requesterID, CreateOrUpdateCustomerDTO customerToUpdateDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(customerToUpdateDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        Customer existingCustomer = company.getCustomers().stream()
                .filter(c -> c.getId().equals(customerToUpdateDTO.id()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Customer not found in this company"));

        if (company.getCustomers().stream().anyMatch(x -> x.getPhone().equals(customerToUpdateDTO.phone()) && !x.getId().equals(existingCustomer.getId())))
            throw new RuntimeException("thisPhoneAlreadyInUse");

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

    public void deleteCustomer(String requesterID, FindCustomerDTO dto) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(dto.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, requester))
            throw new RuntimeException("You are not allowed to see the categories of this company");

        Customer existingCustomer = company.getCustomers().stream()
                .filter(c -> c.getId().equals(dto.customerID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Customer not found in this company"));

        customerRepo.deleteById(existingCustomer.getId());
    }
}
