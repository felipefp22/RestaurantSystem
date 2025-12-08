package com.RestaurantSystem.Controllers.ThirdSuppliersControllers;

import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.ThirdSuppliersService.IFoodService;
import com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs.MerchantDataIFoodDTO;
import com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs.ReceiveCustomerCodeToRegisterIFoodDTO;
import com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs.ReturnIFoodCodeToUserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ifood")
public class IFoodController {
    private final RetriveAuthInfosService retriveAuthInfosService;
    private final IFoodService ifoodService;

    public IFoodController(RetriveAuthInfosService retriveAuthInfosService, IFoodService ifoodService) {
        this.retriveAuthInfosService = retriveAuthInfosService;
        this.ifoodService = ifoodService;
    }


    // <> ------------- Methods ------------- <>
    @GetMapping("/get-connected-ifood-store/{companyID}")
    public ResponseEntity<List<MerchantDataIFoodDTO>> getConnectedIFoodStore(@RequestHeader("Authorization") String authorizationHeader,
                                                                             @PathVariable UUID companyID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        var response = ifoodService.getConnectedIFoodStore(requesterID, companyID);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/create-user-code/{companyID}")
    public ResponseEntity<ReturnIFoodCodeToUserDTO> createUserCode(@RequestHeader("Authorization") String authorizationHeader,
                                                                   @PathVariable UUID companyID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        var response = ifoodService.createUserCode(requesterID, companyID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-authorize-user-code")
    public ResponseEntity registerAuthorizeUserCode(@RequestHeader("Authorization") String authorizationHeader,
                                                    @RequestBody ReceiveCustomerCodeToRegisterIFoodDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        ifoodService.registerAuthorizeUserCode(requesterID, dto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/disconnect-ifood-store/{companyID}")
    public ResponseEntity disconnectIFoodStore(@RequestHeader("Authorization") String authorizationHeader,
                                               @PathVariable UUID companyID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        ifoodService.disconnectIFoodStore(requesterID, companyID);

        return ResponseEntity.ok().build();
    }
}
