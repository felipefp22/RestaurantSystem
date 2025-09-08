package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.Company.DTOs.UpdateCompanyDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import org.springframework.stereotype.Service;

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
        if(companyRepo.findByOwner(requesterID).isPresent()) throw new RuntimeException("This user already have Company: " + requesterID);
        if(companyRepo.findByCompanyName(createCompanyDTO.companyName()).isPresent()) throw new RuntimeException("Already have company with that name.");

        AuthUserLogin owner = authUserRepository.findById(requesterID).orElseThrow(() -> new RuntimeException("User not found"));
        if(owner.getCompanyId() != null) throw new RuntimeException("This user already have Company: " + requesterID);

        Company company = new Company(createCompanyDTO, owner);
        Company companySaved = companyRepo.save(company);
        owner.setCompanyId(String.valueOf(companySaved.getId()));
        authUserRepository.save(owner);

        return company;
    }

    public Company updateCompany(String requesterID, UpdateCompanyDTO updateCompanyDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if(company.getId() != updateCompanyDTO.id()) throw new RuntimeException("You can update only your company");

        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product category, ask to manager");

        company.setCompanyName(updateCompanyDTO.companyName());
        company.setCompanyEmail(updateCompanyDTO.companyEmail());
        company.setCompanyPhone(updateCompanyDTO.companyPhone());
        company.setCompanyAddress(updateCompanyDTO.companyAddress());
        company.setUrlCompanyLogo(updateCompanyDTO.urlCompanyLogo());
        company.setNumberOfTables(updateCompanyDTO.numberOfTables());

        return companyRepo.save(company);
    }
}
