package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CreateOrUpdateCompoundDTO;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompaniesCompoundRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CompaniesCompoundService {

    private final CompaniesCompoundRepo companiesCompoundRepo;
    private final AuthUserRepository authUserRepository;
    private final VerificationsServices verificationsServices;

    public CompaniesCompoundService(CompaniesCompoundRepo companiesCompoundRepo, AuthUserRepository authUserRepository, VerificationsServices verificationsServices) {
        this.companiesCompoundRepo = companiesCompoundRepo;
        this.authUserRepository = authUserRepository;
        this.verificationsServices = verificationsServices;
    }

    // <> ------------- Methods ------------- <>
    public CompaniesCompound createCompaniesCompound(String requesterID, CreateOrUpdateCompoundDTO createCompoundDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);

        if (requester.getCompaniesCompounds().stream()
                .anyMatch(c -> c.getCompoundName().equalsIgnoreCase(createCompoundDTO.compoundName())))
            throw new RuntimeException("You already have a CompaniesCompound with this name");

        CompaniesCompound companiesCompound = new CompaniesCompound(requester, createCompoundDTO);

        return companiesCompoundRepo.save(companiesCompound);
    }

    public CompaniesCompound updateCompaniesCompound(String requesterID, CreateOrUpdateCompoundDTO updateCompoundDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);

        if (requester.getCompaniesCompounds().stream()
                .anyMatch(c -> c.getCompoundName().equalsIgnoreCase(updateCompoundDTO.compoundName()) && !c.getId().equals(updateCompoundDTO.compoundID())))
            throw new RuntimeException("You already have a CompaniesCompound with this name");

        CompaniesCompound companiesCompound = requester.getCompaniesCompounds().stream()
                .filter(c -> c.getId().equals(updateCompoundDTO.compoundID())).findFirst().orElseThrow(() -> new RuntimeException("Compound not found"));

        companiesCompound.setCompoundName(updateCompoundDTO.compoundName());
        companiesCompound.setCompoundDescription(updateCompoundDTO.compoundDescription());

        companiesCompoundRepo.save(companiesCompound);

        return companiesCompound;
    }
}
