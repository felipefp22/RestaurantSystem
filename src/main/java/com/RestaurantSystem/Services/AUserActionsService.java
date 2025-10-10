package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.ENUMs.Role;
import com.RestaurantSystem.Entities.ENUMs.Theme;
import com.RestaurantSystem.Entities.User.AdmDTOs.IsAdmDTO;
import com.RestaurantSystem.Entities.User.AuthUserDTOs.*;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyEmployeesRepo;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AUserActionsService {
    private final AuthUserRepository authUserRepository;
    private final CompanyEmployeesRepo companyEmployeesRepo;

    public AUserActionsService(AuthUserRepository authUserRepository, CompanyEmployeesRepo companyEmployeesRepo) {
        this.authUserRepository = authUserRepository;
        this.companyEmployeesRepo = companyEmployeesRepo;
    }

    // <>--------------- Methodos ---------------<>
    public AuthUserDTO getUserDatas(String requesterID) {
        AuthUserLogin authUserLogin = authUserRepository.findById(requesterID).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        return new AuthUserDTO(authUserLogin);
    }

    public IsAdmDTO isAdmin(String requesterID) throws Exception {
        AuthUserLogin requesterUser = authUserRepository.findById(requesterID).orElseThrow(() -> new Exception("User not found"));


        return new IsAdmDTO((requesterUser.getRole() == Role.ADMIN || requesterUser.getRole() == Role.MASTERADMIN),
                (requesterUser.getRole() == Role.MASTERADMIN));
    }

    public void setOwnAdministrativePassword(String requesterID, SetOwnAdministrativePasswordDTO setOwnAdministrativePasswordDTO) {
        AuthUserLogin authUserLogin = authUserRepository.findById(requesterID).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        authUserLogin.setOwnAdministrativePassword(setOwnAdministrativePasswordDTO.newAdministrativePassword());
        authUserRepository.save(authUserLogin);
    }

    public void quitCompany(String requesterID, UUID companyId) {
        AuthUserLogin authUserLogin = authUserRepository.findById(requesterID).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        CompanyEmployees companiesToQuit = authUserLogin.getWorksAtCompanies().stream().filter(c -> c.getCompany().getId().equals(companyId))
                .findFirst().orElseThrow(() -> new NoSuchElementException("You don't work at this company"));

        authUserLogin.getWorksAtCompanies().remove(companiesToQuit);
        companyEmployeesRepo.delete(companiesToQuit);
    }

    public void setTheme(String requesterID, String themeName) {
        AuthUserLogin authUserLogin = authUserRepository.findById(requesterID).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        authUserLogin.setTheme(Theme.valueOf(themeName));
    }
}
