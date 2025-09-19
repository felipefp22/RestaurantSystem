package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.Company.DTOs.AddOrUpdateEmployeeDTO;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.Company.DTOs.UpdateCompanyDTO;
import com.RestaurantSystem.Entities.Company.EmployeePosition;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyEmployeesRepo;
import com.RestaurantSystem.Repositories.CompanyRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepo companyRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyEmployeesRepo companyEmployeesRepo;

    public CompanyService(CompanyRepo companyRepo, AuthUserRepository authUserRepository, CompanyEmployeesRepo companyEmployeesRepo) {
        this.companyRepo = companyRepo;
        this.authUserRepository = authUserRepository;
        this.companyEmployeesRepo = companyEmployeesRepo;
    }

    // <> ------------- Methods ------------- <>

    public Company createCompany(String requesterID, CreateCompanyDTO createCompanyDTO) {
        AuthUserLogin owner = authUserRepository.findById(requesterID).orElseThrow(() -> new RuntimeException("User not found"));
        CompaniesCompound companiesCompound = owner.getCompaniesCompounds().stream()
                .filter(x -> x.getId().equals(createCompanyDTO.companiesCompoundID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("You are not part of this Companies Compound"));

        if (companiesCompound.getCompanies().stream()
                .anyMatch(c -> c.getCompanyName().equalsIgnoreCase(createCompanyDTO.companyName())))
            throw new RuntimeException("This Companies Compound already has a company with this name");

        Company company = new Company(companiesCompound, createCompanyDTO);
        companyRepo.save(company);

        return company;
    }

    public Company updateCompany(String requesterID, UpdateCompanyDTO updateCompanyDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(updateCompanyDTO.id())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Boolean requesterHavePermission = false;

        if (company.getOwnerCompound().getOwner().equals(requester)) {
            requesterHavePermission = true;
        } else if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.MANAGER))) {
            requesterHavePermission = true;
        }

        if (!requesterHavePermission) throw new RuntimeException("Just Owner or Manager can add employees to a company");

        List<Company> companies = requester.getCompaniesCompounds().stream()
                .flatMap(compound -> compound.getCompanies().stream())
                .filter(c -> c.getId().equals(updateCompanyDTO.id()))
                .toList();

        if (companies.isEmpty())
            throw new RuntimeException("No Company found in your compounds");

        Company companyToUpdate = companies.stream().filter(c -> c.getId().equals(updateCompanyDTO.id())).findFirst()
                .orElseThrow(() -> new RuntimeException("Company not found in your compounds"));

        companyToUpdate.setCompanyName(updateCompanyDTO.companyName());
        companyToUpdate.setCompanyEmail(updateCompanyDTO.companyEmail());
        companyToUpdate.setCompanyPhone(updateCompanyDTO.companyPhone());
        companyToUpdate.setCompanyAddress(updateCompanyDTO.companyAddress());
        companyToUpdate.setUrlCompanyLogo(updateCompanyDTO.urlCompanyLogo());
        companyToUpdate.setNumberOfTables(updateCompanyDTO.numberOfTables());

        return companyRepo.save(companyToUpdate);
    }

    public List<CompanyEmployees> addEmployeeToCompany(String requesterID, AddOrUpdateEmployeeDTO employeeDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(employeeDTO.companyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Boolean requesterHavePermission = false;

        if (company.getOwnerCompound().getOwner().equals(requester)) {
            requesterHavePermission = true;
        } else if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.MANAGER))
                || company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.SUPERVISOR))) {
            requesterHavePermission = true;
        }

        if (!requesterHavePermission) throw new RuntimeException("Just Owner, Supervisor or Manager can add employees to a company");
        AuthUserLogin employeeToAdd = authUserRepository.findById(employeeDTO.employeeEmail())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().getEmail().equals(employeeDTO.employeeEmail())))
            throw new RuntimeException("This employee is already part of this company");

        CompanyEmployees companyEmployee = new CompanyEmployees(company, employeeToAdd, EmployeePosition.valueOf(employeeDTO.position()));

        companyEmployeesRepo.save(companyEmployee);
        company = companyRepo.findById(company.getId()).orElse(null);
        return company.getEmployees();
    }

    public List<CompanyEmployees> removeEmployeeFromCompany(String requesterID, AddOrUpdateEmployeeDTO employeeDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(employeeDTO.companyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Boolean requesterHavePermission = false;

        if (company.getOwnerCompound().getOwner().equals(requester)) {
            requesterHavePermission = true;
        } else if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.MANAGER))
                || company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.SUPERVISOR))) {
            requesterHavePermission = true;
        }

        if (!requesterHavePermission) throw new RuntimeException("Just Owner, Supervisor or Manager can add employees to a company");

        CompanyEmployees companyEmployeeToRemove = company.getEmployees().stream()
                .filter(e -> e.getEmployee().getEmail().equals(employeeDTO.employeeEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("This employee is not part of this company"));

        companyEmployeesRepo.delete(companyEmployeeToRemove);
        company = companyRepo.findById(company.getId()).orElse(null);
        return company.getEmployees();
    }

    public List<CompanyEmployees> updateEmployeePosition(String requesterID, AddOrUpdateEmployeeDTO employeeDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(employeeDTO.companyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Boolean requesterHavePermission = false;

        if (company.getOwnerCompound().getOwner().equals(requester)) {
            requesterHavePermission = true;
        } else if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.MANAGER))
                || company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.SUPERVISOR))) {
            requesterHavePermission = true;
        }

        if (!requesterHavePermission) throw new RuntimeException("Just Owner, Supervisor or Manager can add employees to a company");

        CompanyEmployees companyEmployeeToUpdate = company.getEmployees().stream()
                .filter(e -> e.getEmployee().getEmail().equals(employeeDTO.employeeEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("This employee is not part of this company"));

        companyEmployeeToUpdate.setPosition(EmployeePosition.valueOf(employeeDTO.position()));

        companyEmployeesRepo.save(companyEmployeeToUpdate);
        company = companyRepo.findById(company.getId()).orElse(null);
        return company.getEmployees();
    }
}
