package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.Customer.DTOs.CalculateDistanceAndPriceDTO;
import com.RestaurantSystem.Entities.Customer.DTOs.CreateOrUpdateCustomerDTO;
import com.RestaurantSystem.Entities.Customer.DTOs.FindCustomerDTO;
import com.RestaurantSystem.Entities.Customer.DTOs.ReturnCustomerDistanceAndPriceDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.CustomerRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

import static com.RestaurantSystem.Services.Utils.DeliveryFeeAndDistance.calculateDeliveryRawFee;
import static com.RestaurantSystem.Services.Utils.DeliveryFeeAndDistance.calculateEstimatedKm;

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

    public Set<Customer> getAllCustomers(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.worksOnCompany(company, requester);

        return company.getCustomers();
    }

    public ReturnCustomerDistanceAndPriceDTO calculateAddressDistanceAndPrice(String requesterID, CalculateDistanceAndPriceDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.worksOnCompany(company, requester);

        int distance = calculateEstimatedKm(dto.lat(), dto.lng(), company.getCompanyLat(), company.getCompanyLng());
        Double rawDeliveryFee = calculateDeliveryRawFee(company, distance);

        return new ReturnCustomerDistanceAndPriceDTO(distance, rawDeliveryFee);
    }

    public Customer createCustomer(String requesterID, CreateOrUpdateCustomerDTO customerToCreateDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(customerToCreateDTO.companyID());
        verificationsServices.worksOnCompany(company, requester);

        if (company.getCustomers().stream().anyMatch(x -> x.getPhone().equals(customerToCreateDTO.phone())))
            throw new RuntimeException("thisPhoneAlreadyInUse");

        Customer newCustomer = new Customer(company, customerToCreateDTO);
        saveDeliveryFeeAndDistance(company, newCustomer);

        customerRepo.save(newCustomer);

        return newCustomer;
    }

    public Customer updateCustomer(String requesterID, CreateOrUpdateCustomerDTO customerToUpdateDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(customerToUpdateDTO.companyID());
        verificationsServices.worksOnCompany(company, requester);

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
        existingCustomer.setExtraDeliveryFee(customerToUpdateDTO.extraDeliveryFee());
        saveDeliveryFeeAndDistance(company, existingCustomer);

        Customer savedCustomer = customerRepo.save(existingCustomer);

        return existingCustomer;
    }

    public void deleteCustomer(String requesterID, FindCustomerDTO dto) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(dto.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        Customer existingCustomer = company.getCustomers().stream()
                .filter(c -> c.getId().equals(dto.customerID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Customer not found in this company"));

        customerRepo.deleteById(existingCustomer.getId());
    }


    // <> ---------- Auxs Services ---------- <>
    private void saveDeliveryFeeAndDistance(Company company, Customer customer) {
        if (customer.getLat() != null && customer.getLng() != null) {
            int distance = calculateEstimatedKm(customer.getLat(), customer.getLng(), company.getCompanyLat(), company.getCompanyLng());
            customer.setDistanceFromStoreKM(distance);

            Double deliveryFee = calculateDeliveryRawFee(company, distance);
            customer.setCachedRawDeliveryFee(deliveryFee);
        } else {
            customer.setDistanceFromStoreKM(null);
            customer.setCachedRawDeliveryFee(null);
        }
    }

}
