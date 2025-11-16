package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.Company.EmployeeStatus;
import com.RestaurantSystem.Entities.ENUMs.Role;
import com.RestaurantSystem.Entities.ENUMs.Theme;
import com.RestaurantSystem.Entities.User.AdmDTOs.IsAdmDTO;
import com.RestaurantSystem.Entities.User.AuthUserDTOs.*;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyEmployeesRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AUserActionsService {
    private final AuthUserRepository authUserRepository;
    private final CompanyEmployeesRepo companyEmployeesRepo;
    private final VerificationsServices verificationsServices;

    public AUserActionsService(AuthUserRepository authUserRepository, CompanyEmployeesRepo companyEmployeesRepo, VerificationsServices verificationsServices) {
        this.authUserRepository = authUserRepository;
        this.companyEmployeesRepo = companyEmployeesRepo;
        this.verificationsServices = verificationsServices;
    }

    // <>--------------- Methodos ---------------<>
    @Transactional
    public AuthUserDTO getUserData(String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);

        return new AuthUserDTO(requester);
    }

    public IsAdmDTO isAdmin(String requesterID) throws Exception {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);


        return new IsAdmDTO((requester.getRole() == Role.ADMIN || requester.getRole() == Role.MASTERADMIN),
                (requester.getRole() == Role.MASTERADMIN));
    }

    public void setOwnAdministrativePassword(String requesterID, SetOwnAdministrativePasswordDTO setOwnAdministrativePasswordDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);

        requester.setOwnAdministrativePassword(setOwnAdministrativePasswordDTO.newAdministrativePassword());
        authUserRepository.save(requester);
    }

    public void acceptInviteCompany(String requesterID, UUID companyId) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);

        CompanyEmployees companiesToAccept = requester.getWorksAtCompanies().stream().filter(c -> c.getCompany().getId().equals(companyId))
                .findFirst().orElseThrow(() -> new NoSuchElementException("You don't work at this company"));

        companiesToAccept.setStatus(EmployeeStatus.ACTIVE);

        companyEmployeesRepo.save(companiesToAccept);
    }

    public void quitCompany(String requesterID, UUID companyId) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);

        CompanyEmployees companiesToQuit = requester.getWorksAtCompanies().stream().filter(c -> c.getCompany().getId().equals(companyId))
                .findFirst().orElseThrow(() -> new NoSuchElementException("You don't work at this company"));

        requester.getWorksAtCompanies().remove(companiesToQuit);
        companyEmployeesRepo.delete(companiesToQuit);
    }

    public Theme setTheme(String requesterID, String themeName) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        requester.setTheme(Theme.valueOf(themeName));
        authUserRepository.save(requester);

        return requester.getTheme();
    }
}
