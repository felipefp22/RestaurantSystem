package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import org.springframework.stereotype.Service;

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

}
