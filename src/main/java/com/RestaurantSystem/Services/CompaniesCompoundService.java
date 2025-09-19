package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CreateOrUpdateCompoundDTO;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompaniesCompoundRepo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompaniesCompoundService {

    private final CompaniesCompoundRepo companiesCompoundRepo;
    private final AuthUserRepository authUserRepository;

    public CompaniesCompoundService(CompaniesCompoundRepo companiesCompoundRepo, AuthUserRepository authUserRepository) {
        this.companiesCompoundRepo = companiesCompoundRepo;
        this.authUserRepository = authUserRepository;
    }

    // <> ------------- Methods ------------- <>

    public CompaniesCompound createCompaniesCompound(String requesterID, CreateOrUpdateCompoundDTO createCompoundDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID).orElseThrow(() -> new RuntimeException("User not found"));

            if (requester.getCompaniesCompounds().stream()
                    .anyMatch(c -> c.getCompoundName().equalsIgnoreCase(createCompoundDTO.compoundName())))
                throw new RuntimeException("You already have a CompaniesCompound with this name");

        CompaniesCompound companiesCompound = new CompaniesCompound(requester, createCompoundDTO);

        companiesCompoundRepo.save(companiesCompound);

        return companiesCompound;
    }

    public CompaniesCompound updateCompaniesCompound(String requesterID, UUID compoundID, CreateOrUpdateCompoundDTO updateCompoundDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID).orElseThrow(() -> new RuntimeException("User not found"));

        if (requester.getCompaniesCompounds().stream()
                .anyMatch(c -> c.getCompoundName().equalsIgnoreCase(updateCompoundDTO.compoundName())))
            throw new RuntimeException("You already have a CompaniesCompound with this name");

        CompaniesCompound companiesCompound = requester.getCompaniesCompounds().stream()
                .filter(c -> c.getId().equals(compoundID)).findFirst().orElseThrow(() -> new RuntimeException("Compound not found"));

        companiesCompound.setCompoundName(updateCompoundDTO.compoundName());
        companiesCompound.setCompoundDescription(updateCompoundDTO.compoundDescription());

        companiesCompoundRepo.save(companiesCompound);

        return companiesCompound;
    }
}
