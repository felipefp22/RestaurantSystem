package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.User.AuthUserDTOs.*;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.AuthUserService;
import com.RestaurantSystem.Services.AuxsServices.TokenConfirmationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthUserController {
    private final AuthUserService authUserService;
    private final RetriveAuthInfosService retriveAuthInfosService;
    private final TokenConfirmationService tokenConfirmationService;

    public AuthUserController(AuthUserService authUserService, RetriveAuthInfosService retriveAuthInfosService, TokenConfirmationService tokenConfirmationService) {
        this.authUserService = authUserService;
        this.retriveAuthInfosService = retriveAuthInfosService;
        this.tokenConfirmationService = tokenConfirmationService;
    }


    // <>--------------- Methodos ---------------<>
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO authenticationDTO) {

        LoginResponseDTO loginResponseDTO = authUserService.login(authenticationDTO, null);

        if (loginResponseDTO != null) {
            return ResponseEntity.ok(loginResponseDTO);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@RequestBody @Valid RegisterAuthUserDTO registerAuthUserDTO) throws Exception {

        LoginResponseDTO userRegisteredLoginToReturn = authUserService.createUser(registerAuthUserDTO, null);

        if (userRegisteredLoginToReturn != null) {
            return ResponseEntity.ok(userRegisteredLoginToReturn);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {

        LoginResponseDTO loginResponseDTO = authUserService.refreshToken(refreshTokenDTO);

        if (loginResponseDTO != null) {
            return ResponseEntity.ok(loginResponseDTO);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/logout")
    public ResponseEntity logout(@RequestHeader("Authorization") String authorizationHeader,
                                 @RequestHeader("refreshToken") String refreshToken) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        authUserService.logout(authorizationHeader, refreshToken, null, requesterID);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-if-email-confirmed")
    public ResponseEntity<IsEmailConfirmedDTO> verifyIfEmailConfirmed(@RequestHeader("Authorization") String authorizationHeader) {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        return ResponseEntity.ok(authUserService.verifyIfEmailConfirmed(requesterID));
    }


    @GetMapping("/loginFailure")
    public String loginFailure() {
        return "Login failed!";
    }

    // <>--------------- Tokens Confimations ---------------<>
    @PatchMapping("/request-reset-password")
    public ResponseEntity forgetPassword(@RequestHeader("emailToResetPassword") String emailToResetPassword) throws Exception {


        tokenConfirmationService.createResetPasswordToken(emailToResetPassword);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/request-confirm-account")
    public ResponseEntity<String> requestConfirmAccount(@RequestHeader("Authorization") String authorizationHeader) throws Exception {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        tokenConfirmationService.createAccountConfirmationToken(requesterID);

        return ResponseEntity.ok(requesterID);
    }


    @PatchMapping("/get-token-reset-password/{confirmationCode}")
    public ResponseEntity<TokenToResetPasswordFromCodeDTO> getTokenResetPassword(@RequestHeader("emailToResetPassword") String emailToResetPassword,
                                                                                 @PathVariable("confirmationCode") int confirmationCode) throws InterruptedException {

        return ResponseEntity.ok(tokenConfirmationService.getTokenResetPasswordFromCode(emailToResetPassword, confirmationCode));
    }

    @PutMapping("/reset-password")
    public ResponseEntity resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) throws Exception {

        tokenConfirmationService.confirmationResetPassword(resetPasswordDTO);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/confirm-account")
    public ResponseEntity<IsEmailConfirmedDTO> confirmAccountViaToken(@RequestHeader("token") String token) throws Exception {

        var response = tokenConfirmationService.confirmationCreatedAccountViaToken(token);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/confirm-account-via-code/{confirmationCode}")
    public ResponseEntity<IsEmailConfirmedDTO> confirmAccountViaCode(@RequestHeader("Authorization") String authorizationHeader,
                                                                     @PathVariable("confirmationCode") int confirmationCode) throws Exception {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        var response = tokenConfirmationService.confirmationCreatedAccountViaCode(confirmationCode, requesterID);

        return ResponseEntity.ok(response);
    }

//    @DeleteMapping("/request-delete-account-token")
//    public ResponseEntity requestDeleteAccountToken(@RequestBody @Valid AuthenticationDTO authenticationDTO) throws Exception {
//
//        tokenConfirmationService.requestDeleteAccountToken(authenticationDTO);
//        return ResponseEntity.status(HttpStatus.OK).build();
//
//    }
//
//    @DeleteMapping("/delete-account-via-code/{deleteCode}")
//    public ResponseEntity deleteAccountViaCode(@RequestBody @Valid AuthenticationDTO authenticationDTO,
//                                               @PathVariable("deleteCode") int deleteCode) throws Exception {
//
//        tokenConfirmationService.deleteAccountViaCode(deleteCode, authenticationDTO);
//
//        return ResponseEntity.noContent().build();
//    }

    // Oauth2
    @GetMapping("/oauth2-hiring/callbacks/oauth2")
    public ResponseEntity<LoginResponseDTO> oauth2Callback(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String refreshToken = (String) session.getAttribute("refreshToken");
        String platform = (String) session.getAttribute("platform");

        LoginResponseDTO loginResponseDTO =
                authUserService.refreshToken(new RefreshTokenDTO(UUID.fromString(refreshToken), "SocialLogin", null));

        if (loginResponseDTO != null) {
            String uriToReturn = "deliverysystem//auth/callback?";
            if (platform != null && platform.equals("prod")) uriToReturn = "https://deliverysystem.com.br/oauthredirect?";
//            if (platform != null && platform.equals("dev")) uriToReturn = "https://desenvolvimento.deliverysystem.com.br/auth/callback?";
            if (platform != null && platform.equals("localhost")) uriToReturn = "http://localhost:5173/oauthredirect?";


            String redirectUrl =
                    uriToReturn +
                    "access_token=" + loginResponseDTO.access_token() +
                    "&refresh_token=" + loginResponseDTO.refresh_token() +
                    "&isEmailConfirmed=" + loginResponseDTO.isEmailConfirmed() +
                    "&isPhoneConfirmed=" + loginResponseDTO.isPhoneConfirmed();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
