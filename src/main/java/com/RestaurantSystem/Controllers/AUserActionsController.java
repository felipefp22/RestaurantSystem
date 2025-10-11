package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.ENUMs.Theme;
import com.RestaurantSystem.Entities.User.AdmDTOs.IsAdmDTO;
import com.RestaurantSystem.Entities.User.AuthUserDTOs.*;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.AUserActionsService;
import com.RestaurantSystem.Services.TemporaryServices.DemonstrationSiteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user-actions")
public class AUserActionsController {
    private final AUserActionsService aUserActionsService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    private final DemonstrationSiteService demonstrationSiteService;

    public AUserActionsController(AUserActionsService aUserActionsService, RetriveAuthInfosService retriveAuthInfosService, DemonstrationSiteService demonstrationSiteService) {
        this.aUserActionsService = aUserActionsService;
        this.retriveAuthInfosService = retriveAuthInfosService;
        this.demonstrationSiteService = demonstrationSiteService;
    }


    // <>--------------- Methodos ---------------<>
    @GetMapping("/me")
    public ResponseEntity<AuthUserDTO> getUserDatas(@RequestHeader("Authorization") String authorizationHeader) throws Exception {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        return ResponseEntity.ok(aUserActionsService.getUserDatas(requesterID));
    }

    @GetMapping("/is-admin")
    public ResponseEntity<IsAdmDTO> isAdmin(@RequestHeader("Authorization") String authorizationHeader) throws Exception {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        return ResponseEntity.ok(aUserActionsService.isAdmin(requesterID));
    }

    @PutMapping("/set-own-administrative-password")
    public ResponseEntity setOwnAdministrativePassword(@RequestHeader("Authorization") String authorizationHeader,
                                                      @RequestBody SetOwnAdministrativePasswordDTO setOwnAdministrativePasswordDTO) throws Exception {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        aUserActionsService.setOwnAdministrativePassword(requesterID, setOwnAdministrativePasswordDTO);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/quit-company/{companyId}")
    public ResponseEntity quitCompany(@RequestHeader("Authorization") String authorizationHeader,
                                      @PathVariable("companyId") UUID companyId) throws Exception {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        aUserActionsService.quitCompany(requesterID, companyId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/set-theme/{themeName}")
    public ResponseEntity<Theme> setTheme(@RequestHeader("Authorization") String authorizationHeader,
                                          @PathVariable("themeName") String themeName) throws Exception {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = aUserActionsService.setTheme(requesterID, themeName);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/create-default-api-demonstration")
    public ResponseEntity createDefaultApiDemonstration(@RequestHeader("Authorization") String authorizationHeader) throws Exception {

        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        demonstrationSiteService.createACompoundAndCompany(requesterID);

        return ResponseEntity.noContent().build();
    }

}
