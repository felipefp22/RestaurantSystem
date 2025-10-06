package com.RestaurantSystem.Services.AuxsServices;

import com.RestaurantSystem.Entities.User.AuthUserDTOs.AuthenticationDTO;
import com.RestaurantSystem.Entities.User.AuthUserDTOs.IsEmailConfirmedDTO;
import com.RestaurantSystem.Entities.User.AuthUserDTOs.ResetPasswordDTO;
import com.RestaurantSystem.Entities.User.AuthUserDTOs.TokenToResetPasswordFromCodeDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Entities.User.RefreshToken;
import com.RestaurantSystem.Entities.User.TokenConfirmation;
import com.RestaurantSystem.Infra.auth.TokenServiceOur;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.RefreshTokenRepository;
import com.RestaurantSystem.Repositories.TokenConfirmationRepository;
import com.RestaurantSystem.Services.TemporaryServices.DemonstrationSiteService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenConfirmationService {
    private final TokenConfirmationRepository tokenConfirmationRepository;
    private final AuthUserRepository authUserRepository;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenServiceOur tokenServiceOur;

    public TokenConfirmationService(TokenConfirmationRepository tokenConfirmationRepository, AuthUserRepository authUserRepository, EmailService emailService,
                                    RefreshTokenRepository refreshTokenRepository, TokenServiceOur tokenServiceOur) {
        this.tokenConfirmationRepository = tokenConfirmationRepository;
        this.authUserRepository = authUserRepository;
        this.emailService = emailService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenServiceOur = tokenServiceOur;
    }

    @Transactional
    public void createResetPasswordToken(String userToChangeID) throws Exception {

        AuthUserLogin userToChange = authUserRepository.findById(userToChangeID)
                .orElseThrow(() -> new Exception("EmailNotFound"));

        List<TokenConfirmation> tokensAntigosDeletar = tokenConfirmationRepository.findAllByUserToChangeID(userToChangeID);
        tokensAntigosDeletar.forEach(x -> {
            if (x.getAction().equals("resetPassword")) tokenConfirmationRepository.delete(x);
        });

        TokenConfirmation token =
                new TokenConfirmation(
                        "resetPassword",
                        userToChangeID,
                        ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusMinutes(10),
                        ""
                );

        tokenConfirmationRepository.save(token);

        emailService.sendSimpleEmailResendsite(
                userToChangeID,
                "Restaurant Delivery - Reset Password",
                "EmailsTemplate/CodeEmailTemplate.html",
                Map.of("title", "Restaurant Delivery - Reset Password",
                        "body", "Reset Password Code: \n\n" + token.getConfirmationCode(),
                        "buttonUrl", " ",
                        "buttonText", "click"));
    }

    @Transactional
    public void createAccountConfirmationToken(String userToChangeID) throws Exception {
        AuthUserLogin userToChange = authUserRepository.findById(userToChangeID)
                .orElseThrow(() -> new Exception("User not found"));
        if (userToChange.isEmailConfirmed()) throw new RuntimeException("Email já confirmado");

        List<TokenConfirmation> tokensAntigosDeletar = tokenConfirmationRepository.findAllByUserToChangeID(userToChangeID);
        tokensAntigosDeletar.forEach(x -> {
            if (x.getAction().equals("accountConfirmation")) tokenConfirmationRepository.delete(x);
        });

        TokenConfirmation token =
                new TokenConfirmation(
                        "accountConfirmation",
                        userToChangeID,
                        ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusDays(1),
                        ""
                );

        tokenConfirmationRepository.save(token);

        try {
            emailService.sendSimpleEmailResendsite(
                    userToChangeID,
                    "Restaurant Delivery - Email Confirmation",
                    "EmailsTemplate/CodeEmailTemplate.html",
                    Map.of("title", "Restaurant Delivery - Email Confirmation",
                            "body", "Confirmation Code: \n\n" + token.getConfirmationCode(),
                            "buttonUrl", " ",
                            "buttonText", "click"));
        } catch (Exception e) {
            System.out.println("[ Envio de email de confirmação de Email ] - Erro ao enviar email");
        }
    }


    @Transactional
    public TokenToResetPasswordFromCodeDTO getTokenResetPasswordFromCode(String emailToResetPassword, int confirmationCode) {
        TokenConfirmation token = tokenConfirmationRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new RuntimeException("expiredToken"));

        if (!isTokenValid(token)) throw new RuntimeException("expiredToken");
        if (!token.getAction().equals("resetPassword")) throw new RuntimeException("expiredToken");
        if (!token.getUserToChangeID().equals(emailToResetPassword))
            throw new RuntimeException("Email não corresponde ao token");

        return new TokenToResetPasswordFromCodeDTO(token.getId().toString());
    }

    @Transactional
    public void confirmationResetPassword(ResetPasswordDTO passwordDTO) throws Exception {
        TokenConfirmation token = tokenConfirmationRepository.findById(UUID.fromString(passwordDTO.tokenID()))
                .orElseThrow(() -> new RuntimeException("Token não encontrado ou expirado"));

        if (!isTokenValid(token)) throw new RuntimeException("expiredToken");

        if (!token.getAction().equals("resetPassword")) throw new RuntimeException("invalidToken");
        if (passwordDTO.newPassword().equals(passwordDTO.confirmPassword()) && token.getAction().equals("resetPassword")) {
            AuthUserLogin userToChange = authUserRepository.findById(token.getUserToChangeID())
                    .orElseThrow(() -> new Exception("User not found"));

            userToChange.setPassword(passwordDTO.newPassword());

            authUserRepository.save(userToChange);
            tokenConfirmationRepository.delete(token);

            List<RefreshToken> userRefreshTokens = refreshTokenRepository.findByUser(userToChange).orElse(null);
            if (userRefreshTokens != null) {
                userRefreshTokens.forEach(x -> refreshTokenRepository.delete(x));
            }
        }
    }

    public IsEmailConfirmedDTO confirmationCreatedAccountViaToken(String tokenID) throws Exception {
        TokenConfirmation token = tokenConfirmationRepository.findById(UUID.fromString(tokenID))
                .orElseThrow(() -> new RuntimeException("Token não encontrado ou expirado"));

        if (!isTokenValid(token)) throw new RuntimeException("expiredToken");

        if (!token.getAction().equals("accountConfirmation")) throw new RuntimeException("invalidToken");
        if (token.getAction().equals("accountConfirmation")) {
            AuthUserLogin userToChange = authUserRepository.findById(token.getUserToChangeID())
                    .orElseThrow(() -> new Exception("User not found"));

            userToChange.comfirmEmail();
            authUserRepository.save(userToChange);
            tokenConfirmationRepository.delete(token);
            return new IsEmailConfirmedDTO(userToChange.isEmailConfirmed());
        }
        return null;
    }

    @Transactional
    public IsEmailConfirmedDTO confirmationCreatedAccountViaCode(int code, String requesterId) throws Exception {
        TokenConfirmation token = tokenConfirmationRepository.findByConfirmationCode(code)
                .orElseThrow(() -> new RuntimeException("Token não encontrado ou expirado"));

        if (!isTokenValid(token)) throw new RuntimeException("expiredToken");
        if (!token.getUserToChangeID().equals(requesterId)) throw new RuntimeException("invalidToken");

        if (!token.getAction().equals("accountConfirmation")) throw new RuntimeException("invalidToken");
        if (token.getAction().equals("accountConfirmation")) {
            AuthUserLogin userToChange = authUserRepository.findById(token.getUserToChangeID())
                    .orElseThrow(() -> new Exception("User not found"));

            if (userToChange.getEmail().equals(requesterId)) {

                userToChange.comfirmEmail();
                authUserRepository.save(userToChange);
                tokenConfirmationRepository.delete(token);
                return new IsEmailConfirmedDTO(userToChange.isEmailConfirmed());
            }
        }
        return null;
    }

    @Transactional
    public void requestDeleteAccountToken(AuthenticationDTO authenticationDTO) throws Exception {
        AuthUserLogin userToDel = authUserRepository.findById(authenticationDTO.emailOrUsername())
                .orElseThrow(() -> new RuntimeException("loginIncorrect"));

        if (!new BCryptPasswordEncoder().matches(authenticationDTO.password(), userToDel.getPassword())) {
            throw new RuntimeException("loginIncorrect");
        }

        List<TokenConfirmation> tokensAntigosDeletar = tokenConfirmationRepository.findAllByUserToChangeID(userToDel.getEmail());
        tokensAntigosDeletar.forEach(x -> {
            if (x.getAction().equals("deleteAccount")) tokenConfirmationRepository.delete(x);
        });

        TokenConfirmation token =
                new TokenConfirmation(
                        "deleteAccount",
                        userToDel.getEmail(),
                        ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime().plusMinutes(10),
                        ""
                );

        tokenConfirmationRepository.save(token);

        emailService.sendSimpleEmailResendsite(
                userToDel.getEmail(),
                "Restaurant Delivery - Deleção de Conta",
                "EmailsTemplate/CodeEmailTemplate.html",
                Map.of("title", "Restaurant Delivery - Delete Account",
                        "body", "Deletion code: \n\n" + token.getConfirmationCode(),
                        "buttonUrl", " ",
                        "buttonText", "click"));
    }


    // <>--------------- Rotinas ---------------<>
    @Scheduled(fixedRate = 30000)
    public void cleanExpiredTokens() {
        tokenConfirmationRepository.findAll().forEach(token -> {
            if (!isTokenValid(token)) tokenConfirmationRepository.delete(token);
        });
    }


    // <>--------------- Métodos Auxiliares ---------------<>
    private boolean isTokenValid(TokenConfirmation token) {

        return token.getExpirationDate()
                .isAfter(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    }
}
