package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CompoundResumeDTO;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.Company.DTOs.CompanyResumeDTO;
import com.RestaurantSystem.Entities.ENUMs.Role;
import com.RestaurantSystem.Entities.User.AdmDTOs.IsAdmDTO;
import com.RestaurantSystem.Entities.User.AuthUserDTOs.*;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Entities.User.RefreshToken;
import com.RestaurantSystem.Infra.Exceptions.ExceptionsToThrow.EmailAlreadyConfirmedException;
import com.RestaurantSystem.Infra.auth.TokenServiceOur;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyEmployeesRepo;
import com.RestaurantSystem.Repositories.RefreshTokenRepository;
import com.RestaurantSystem.Services.TemporaryServices.DemonstrationSiteService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthUserService {
    private final AuthUserRepository authUserRepository;
    private final RefreshTokenRepository refreshTokenRepo;
    private final TokenServiceOur tokenServiceOur;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final CompanyEmployeesRepo companyEmployeesRepo;


    public AuthUserService(AuthUserRepository authUserRepository, RefreshTokenRepository refreshTokenRepo, TokenServiceOur tokenServiceOur, CompanyEmployeesRepo companyEmployeesRepo, DemonstrationSiteService demonstrationSiteService) {
        this.authUserRepository = authUserRepository;
        this.refreshTokenRepo = refreshTokenRepo;
        this.tokenServiceOur = tokenServiceOur;
        this.companyEmployeesRepo = companyEmployeesRepo;
    }

    // <>--------------- Methodos ---------------<>
    public AuthUserDTO getUserDatas(String requesterID) {
        AuthUserLogin authUserLogin = findUsuarioByEmail(requesterID).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        return new AuthUserDTO(authUserLogin);
    }

    public IsAdmDTO isAdmin(String requesterID) throws Exception {
        AuthUserLogin requesterUser = authUserRepository.findById(requesterID).orElseThrow(() -> new Exception("User not found"));


        return new IsAdmDTO((requesterUser.getRole() == Role.ADMIN || requesterUser.getRole() == Role.MASTERADMIN),
                (requesterUser.getRole() == Role.MASTERADMIN));
    }

    @Transactional
    public LoginResponseDTO login(AuthenticationDTO authenticationDTO, String fcmToken) {
        AuthUserLogin user;
        if (isEmail(authenticationDTO.emailOrUsername())) {
            user = authUserRepository.findById(authenticationDTO.emailOrUsername())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        } else {
            throw new RuntimeException("Usuário não encontrado");
        }
//        else {
//            user = authUserRepository.findByUsernameIgnoreCase(authenticationDTO.emailOrUsername())
//                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
//        }

        if (!new BCryptPasswordEncoder().matches(authenticationDTO.password(), user.getPassword())) {
            throw new RuntimeException("Login informations incorrect");
        }

        String token = tokenServiceOur.generateToken(user);

        List<CompoundResumeDTO> coumpoundsYouAreOwner = user.getCompaniesCompounds().stream().map(CompoundResumeDTO::new).toList();
        List<CompanyResumeDTO> companiesYouWorks = user.getWorksAtCompanies().stream().map(CompanyEmployees::getCompany).toList().stream().map(CompanyResumeDTO::new).toList();

        return new LoginResponseDTO(user, token,
                tokenServiceOur.createRefreshToken(user, token).getId(), user.isEmailConfirmed(), false, coumpoundsYouAreOwner, companiesYouWorks);
    }

    @Transactional
    public LoginResponseDTO createUser(RegisterAuthUserDTO registerAuthUserDTO, String fcmToken) throws EmailAlreadyConfirmedException {
        verifyIfUserExists(registerAuthUserDTO);
        if (authUserRepository.existsById(registerAuthUserDTO.email())
//                || authUserRepository.existsByUsername(registerAuthUserDTO.username())
        ) throw new EmailAlreadyConfirmedException("EmailAlreadyOnUse");

        AuthUserLogin authUserLogin = new AuthUserLogin(registerAuthUserDTO);
        AuthUserLogin newUserRegistered = authUserRepository.save(authUserLogin);

        AuthenticationDTO authenticationDTO = new AuthenticationDTO(newUserRegistered.getEmail(), registerAuthUserDTO.password());

        return login(authenticationDTO, fcmToken);
    }

    @Transactional
    public LoginResponseDTO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        RefreshToken refreshToken = tokenServiceOur.findRefreshTokenByToken(refreshTokenDTO.refreshToken());
        AuthUserLogin user = refreshToken.getUser();

        tokenServiceOur.deleteRefreshToken(refreshTokenDTO.refreshToken());

        if (refreshToken.getAssociatedToken().equals(refreshTokenDTO.associatedToken())) {

            String token = tokenServiceOur.generateToken(user);

            List<CompoundResumeDTO> coumpoundsYouAreOwner = user.getCompaniesCompounds().stream().map(CompoundResumeDTO::new).toList();
            List<CompanyResumeDTO> companiesYouWorks = user.getWorksAtCompanies().stream().map(CompanyEmployees::getCompany).toList().stream().map(CompanyResumeDTO::new).toList();

            return new LoginResponseDTO(user, token,
                    tokenServiceOur.createRefreshToken(user, token).getId(), user.isEmailConfirmed(), false, coumpoundsYouAreOwner, companiesYouWorks);
        }

        return null;
    }

    public void logout(String jwt, String refreshToken, String fcmToken, String requesterID) {
        AuthUserLogin authUserLogin = findUsuarioByEmail(requesterID).orElse(null);

        tokenServiceOur.deleteRefreshToken(UUID.fromString(refreshToken));

    }

    public IsEmailConfirmedDTO verifyIfEmailConfirmed(String requesterID) {
        AuthUserLogin authUserLogin = findUsuarioByEmail(requesterID).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        return new IsEmailConfirmedDTO(authUserLogin.isEmailConfirmed());
    }

    public void setOwnAdministrativePassword(String requesterID, SetOwnAdministrativePasswordDTO setOwnAdministrativePasswordDTO) {
        AuthUserLogin authUserLogin = findUsuarioByEmail(requesterID).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        authUserLogin.setOwnAdministrativePassword(setOwnAdministrativePasswordDTO.newAdministrativePassword());
        authUserRepository.save(authUserLogin);
    }

    public void quitCompany(String requesterID, UUID companyId) {
        AuthUserLogin authUserLogin = findUsuarioByEmail(requesterID).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        CompanyEmployees companiesToQuit = authUserLogin.getWorksAtCompanies().stream().filter(c -> c.getCompany().getId().equals(companyId))
                .findFirst().orElseThrow(() -> new NoSuchElementException("You don't work at this company"));

        authUserLogin.getWorksAtCompanies().remove(companiesToQuit);
        companyEmployeesRepo.delete(companiesToQuit);
    }

//    public String updateRole(String role, AuthUser authUser) {
//        authUser.setRole(Role.valueOf(role.toUpperCase()));
//
//        return userLoginRepository.updateRole(authUser).getPartitionKey() + " now is: " + role.toUpperCase();
//    }

    // <>--------------- Methodos Auxiliares ---------------<>

    public void verifyIfUserExists(RegisterAuthUserDTO registerAuthUserDTO) {
        authUserRepository.findById(registerAuthUserDTO.email()).ifPresent(user -> {
            throw new EmailAlreadyConfirmedException("Email on use");
        });
//        authUserRepository.findByUsernameIgnoreCase(registerAuthUserDTO.username()).ifPresent(usuario -> {
//            throw new RuntimeException("Username is not available");
//        });
    }

    public boolean isThisEmailAvailable(String email) {
        AuthUserLogin userFound = authUserRepository.findById(email).orElse(null);
        if (userFound == null) return true;

        return false;
    }
//    public boolean isThisUsernameAvailable(String username) {
//        AuthUserLogin userFound = authUserRepository.findByUsernameIgnoreCase(username).orElse(null);
//        if (userFound == null) return true;
//
//        return false;
//    }


    private boolean isEmail(String login) {
        // Basic email pattern check
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return login.matches(emailPattern);
    }

    public Optional<AuthUserLogin> findUsuarioByEmail(String email) {

        Optional<AuthUserLogin> userLogin = authUserRepository.findById(email);

        if (userLogin.isEmpty()) throw new NoSuchElementException("Usuário não encontrado");
        return userLogin;
    }



//    public Optional<AuthUserLogin> findUsuarioByUsername(String username) {
//
//        Optional<AuthUserLogin> userLogin = authUserRepository.findByUsernameIgnoreCase(username);
//
//        if (userLogin.isEmpty()) throw new NoSuchElementException("Usuário não encontrado");
//        return userLogin;
//    }

}
