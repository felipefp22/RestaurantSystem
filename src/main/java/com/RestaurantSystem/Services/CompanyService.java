package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.Company.DTOs.UpdateCompanyDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepo companyRepo;
    private final AuthUserRepository authUserRepository;


    public CompanyService(CompanyRepo companyRepo, AuthUserRepository authUserRepository) {
        this.companyRepo = companyRepo;
        this.authUserRepository = authUserRepository;
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

        if(requester.getCompaniesCompounds().isEmpty())
            throw new RuntimeException("Just Owner can update a company");

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
}
