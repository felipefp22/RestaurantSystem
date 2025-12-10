package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.Company.DTOs.*;
import com.RestaurantSystem.Entities.Company.EmployeePosition;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyEmployeesRepo;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ShiftRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepo companyRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyEmployeesRepo companyEmployeesRepo;
    private final VerificationsServices verificationsServices;
    private final ShiftRepo shiftRepo;

    public CompanyService(CompanyRepo companyRepo, AuthUserRepository authUserRepository, CompanyEmployeesRepo companyEmployeesRepo, VerificationsServices verificationsServices, ShiftRepo shiftRepo) {
        this.companyRepo = companyRepo;
        this.authUserRepository = authUserRepository;
        this.companyEmployeesRepo = companyEmployeesRepo;
        this.verificationsServices = verificationsServices;
        this.shiftRepo = shiftRepo;
    }

    // <> ------------- Methods ------------- <>
    public CompanyOperationDTO getCompanyOperation(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.worksOnCompany(company, requester);

        Shift currentShift = verificationsServices.retrieveCurrentShift(company);

        if (verificationsServices.isDeliveryman(company, requester)) {
            CompanyOperationDeliveryManDTO dto = new CompanyOperationDeliveryManDTO(company, currentShift, requesterID);
            return new CompanyOperationDTO(dto);
        }

        return new CompanyOperationDTO(company, currentShift);
    }

    public Company createCompany(String requesterID, CreateCompanyDTO createCompanyDTO) {
        AuthUserLogin owner = verificationsServices.retrieveRequester(requesterID);

        if (owner.getCompaniesCompounds().isEmpty())
            throw new RuntimeException("You need to have at least one Companies Compound to create a company");

        CompaniesCompound companiesCompound = owner.getCompaniesCompounds().stream()
                .filter(x -> x.getId().equals(createCompanyDTO.companiesCompoundID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("You are not part of this Companies Compound"));

        if (companiesCompound.getCompanies().stream()
                .anyMatch(c -> c.getCompanyName().equalsIgnoreCase(createCompanyDTO.companyName())))
            throw new RuntimeException("This Companies Compound already has a company with this name");

        Company company = new Company(companiesCompound, createCompanyDTO);
        company = companyRepo.save(company);
        addNoUserDeliveryman(requesterID, new AddOrRemoveNoUserDeliveryManDTO(company.getId(), "MotoBoy 01"));

        return company;
    }

    public Company updateCompany(String requesterID, UpdateCompanyDTO updateCompanyDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(updateCompanyDTO.companyID());
        verificationsServices.justOwner(company, requester);

        if (company.getOwnerCompound().getCompanies().stream()
                .anyMatch(c -> c.getCompanyName().equalsIgnoreCase(updateCompanyDTO.companyName()) && !c.getId().equals(updateCompanyDTO.companyID())))
            throw new RuntimeException("This Companies Compound already has a company with this name");

        Company companyToUpdate = requester.getCompaniesCompounds().stream()
                .flatMap(compound -> compound.getCompanies().stream())
                .filter(c -> c.getId().equals(updateCompanyDTO.companyID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("You are not part of this Companies Compound"));

        companyToUpdate.setCompanyName(updateCompanyDTO.companyName());
        companyToUpdate.setCompanyEmail(updateCompanyDTO.companyEmail());
        companyToUpdate.setCompanyPhone(updateCompanyDTO.companyPhone());
        companyToUpdate.setCompanyAddress(updateCompanyDTO.companyAddress());
        companyToUpdate.setCompanyLat(updateCompanyDTO.companyLat());
        companyToUpdate.setCompanyLng(updateCompanyDTO.companyLng());
        companyToUpdate.setUrlCompanyLogo(updateCompanyDTO.urlCompanyLogo());
        companyToUpdate.setNumberOfTables(updateCompanyDTO.numberOfTables());
        if (updateCompanyDTO.taxServicePercentage() != null)
            companyToUpdate.setTaxServicePercentage(updateCompanyDTO.taxServicePercentage());
        if (updateCompanyDTO.deliveryHasServiceTax() != null)
            companyToUpdate.setDeliveryHasServiceTax(updateCompanyDTO.deliveryHasServiceTax());
        if (updateCompanyDTO.pickupHasServiceTax() != null)
            companyToUpdate.setPickupHasServiceTax(updateCompanyDTO.pickupHasServiceTax());

        companyToUpdate.setMaxRecommendedDistanceKM(updateCompanyDTO.maxRecommendedDistanceKM());
        companyToUpdate.setMaxDeliveryDistanceKM(updateCompanyDTO.maxDeliveryDistanceKM());
        companyToUpdate.setBaseDeliveryDistanceKM(updateCompanyDTO.baseDeliveryDistanceKM());
        companyToUpdate.setBaseDeliveryTax(updateCompanyDTO.baseDeliveryTax());
        companyToUpdate.setTaxPerExtraKM(updateCompanyDTO.taxPerExtraKM());

        return companyRepo.save(companyToUpdate);
    }

    public Company setCompanyGeoLocation(String requesterID, UpdateCompanyDTO updateCompanyDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(updateCompanyDTO.companyID());
        verificationsServices.justOwnerOrManager(company, requester);

        company.setCompanyLat(updateCompanyDTO.companyLat());
        company.setCompanyLng(updateCompanyDTO.companyLng());

        return companyRepo.save(company);
    }

    public List<CompanyEmployeesDTO> getEmployees(String requesterID, String companyID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(companyID))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        return company.getEmployees().stream().map(CompanyEmployeesDTO::new).toList();
    }

    public List<CompanyEmployees> addEmployeeToCompany(String requesterID, AddOrUpdateEmployeeDTO employeeDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(employeeDTO.companyId());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        AuthUserLogin employeeToAdd = authUserRepository.findById(employeeDTO.employeeEmail())
                .orElseThrow(() -> new RuntimeException("emailNotFound"));

        if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().getEmail().equals(employeeDTO.employeeEmail())) || company.getOwnerCompound().getOwner().getEmail().equals(employeeDTO.employeeEmail()))
            throw new RuntimeException("employeeAlreadyHired");

        CompanyEmployees companyEmployee = new CompanyEmployees(company, employeeToAdd, EmployeePosition.valueOf(employeeDTO.position()));

        companyEmployeesRepo.save(companyEmployee);
        company = companyRepo.findById(company.getId()).orElse(null);
        return company.getEmployees();
    }

    public List<CompanyEmployees> updateEmployeePosition(String requesterID, AddOrUpdateEmployeeDTO employeeDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(employeeDTO.companyId());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        CompanyEmployees companyEmployeeToUpdate = company.getEmployees().stream()
                .filter(e -> e.getEmployee().getEmail().equals(employeeDTO.employeeEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("This employee is not part of this company"));

        if (companyEmployeeToUpdate.getPosition().equals(EmployeePosition.valueOf(employeeDTO.position())))
            throw new RuntimeException("This employee already has this position " + employeeDTO.position());

        companyEmployeeToUpdate.setPosition(EmployeePosition.valueOf(employeeDTO.position()));

        companyEmployeesRepo.save(companyEmployeeToUpdate);
        company = companyRepo.findById(company.getId()).orElse(null);
        return company.getEmployees();
    }

    public List<CompanyEmployees> removeEmployeeFromCompany(String requesterID, AddOrUpdateEmployeeDTO employeeDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(employeeDTO.companyId());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        CompanyEmployees companyEmployeeToRemove = company.getEmployees().stream()
                .filter(e -> e.getEmployee().getEmail().equals(employeeDTO.employeeEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("This employee is not part of this company"));

        company.getEmployees().remove(companyEmployeeToRemove);
        companyEmployeesRepo.deleteById(companyEmployeeToRemove.getId());

        company = companyRepo.findById(company.getId()).orElse(null);
        return company.getEmployees();
    }

    public void addNoUserDeliveryman(String requesterID, AddOrRemoveNoUserDeliveryManDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        if (company.getNoUserDeliveryMans().contains(dto.noUserDeliveryMan()))
            throw new RuntimeException("This no user deliveryman already exists");

        company.addNoUserDeliveryMan(dto.noUserDeliveryMan());
        companyRepo.save(company);
    }

    public void removeNoUserDeliveryman(String requesterID, AddOrRemoveNoUserDeliveryManDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        company.removeNoUserDeliveryMan(dto.noUserDeliveryMan());
        companyRepo.save(company);
    }
}
