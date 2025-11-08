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
    public CompanyOperationDTO getCompanyOperation(String requesterID, String companyID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID).orElseThrow(() -> new RuntimeException("User not found"));

        Company company = companyRepo.findById(UUID.fromString(companyID)).orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You don't have permission to access this company");

        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
//        if(openedShift.isEmpty()){
//            throw new RuntimeException("noActiveShift");
//        }
        Shift currentShift = null;
        if(openedShift.size() > 1){
            Shift lastShift = openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
            currentShift = lastShift;
        } else if(openedShift.size() > 0) {
            currentShift = openedShift.get(0);
        };

        if(currentShift == null) currentShift = company.getLastOrOpenShift();

        return new CompanyOperationDTO(company, currentShift);
    }

    public Company createCompany(String requesterID, CreateCompanyDTO createCompanyDTO) {
        AuthUserLogin owner = authUserRepository.findById(requesterID).orElseThrow(() -> new RuntimeException("User not found"));

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

        return companyRepo.save(company);
    }

    public Company updateCompany(String requesterID, UpdateCompanyDTO updateCompanyDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(updateCompanyDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwner(company, requester)) throw new RuntimeException("justOwnerCanEditCompany");

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
        companyToUpdate.setUrlCompanyLogo(updateCompanyDTO.urlCompanyLogo());
        companyToUpdate.setNumberOfTables(updateCompanyDTO.numberOfTables());

        return companyRepo.save(companyToUpdate);
    }

    public Company setCompanyGeoLocation(String requesterID, UpdateCompanyDTO updateCompanyDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(updateCompanyDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManager(company, requester)) throw new RuntimeException("Just Owner or Manager can add employees to a company");

        company.setCompanyLat(updateCompanyDTO.companyLat());
        company.setCompanyLng(updateCompanyDTO.companyLng());

        return companyRepo.save(company);
    }

    public List<CompanyEmployeesDTO> getEmployees(String requesterID, String companyID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(companyID))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, requester)) throw new RuntimeException("Just Owner, Supervisor or Manager can add employees to a company");

        return company.getEmployees().stream().map(CompanyEmployeesDTO::new).toList();
    }

    public List<CompanyEmployees> addEmployeeToCompany(String requesterID, AddOrUpdateEmployeeDTO employeeDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("requesterNotFound"));

        Company company = companyRepo.findById(employeeDTO.companyId())
                .orElseThrow(() -> new RuntimeException("companyNotFound"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, requester)) throw new RuntimeException("Just Owner, Supervisor or Manager can add employees to a company");

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
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(employeeDTO.companyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, requester)) throw new RuntimeException("Just Owner, Supervisor or Manager can add employees to a company");

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
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(employeeDTO.companyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, requester)) throw new RuntimeException("Just Owner, Supervisor or Manager can add employees to a company");

        CompanyEmployees companyEmployeeToRemove = company.getEmployees().stream()
                .filter(e -> e.getEmployee().getEmail().equals(employeeDTO.employeeEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("This employee is not part of this company"));

        company.getEmployees().remove(companyEmployeeToRemove);
        companyEmployeesRepo.deleteById(companyEmployeeToRemove.getId());

        company = companyRepo.findById(company.getId()).orElse(null);
        return company.getEmployees();
    }
}
