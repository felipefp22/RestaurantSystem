//package com.RestaurantSystem.Controllers.ThirdSuppliersControllers;
//
//import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
//import com.RestaurantSystem.Services.ThirdSuppliersService.IFoodService;
//import com.RestaurantSystem.Services.ThirdSuppliersService.NineNineService;
//import com.RestaurantSystem.Services.WebRequests.IFoodDTOs.IFoodMerchantDataDTO;
//import com.RestaurantSystem.Services.WebRequests.IFoodDTOs.ReceiveCustomerCodeToRegisterIFoodDTO;
//import com.RestaurantSystem.Services.WebRequests.IFoodDTOs.ReturnIFoodCodeToUserDTO;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/ninenine")
//public class NineNineController {
//    private final RetriveAuthInfosService retriveAuthInfosService;
//    private final NineNineService nineNineService;
//
//    public NineNineController(RetriveAuthInfosService retriveAuthInfosService, NineNineService nineNineService) {
//        this.retriveAuthInfosService = retriveAuthInfosService;
//        this.nineNineService = nineNineService;
//    }
//
//
//    // <> ------------- Methods ------------- <>
//    @GetMapping("/get-connected-ninenine-store/{companyID}")
//    public ResponseEntity<List<IFoodMerchantDataDTO>> getConnectedNineNineStore(@RequestHeader("Authorization") String authorizationHeader,
//                                                                             @PathVariable UUID companyID) {
//        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
//        var response = nineNineService.getConnectedIFoodStore(requesterID, companyID);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/create-user-code/{companyID}")
//    public ResponseEntity<ReturnIFoodCodeToUserDTO> createUserCode(@RequestHeader("Authorization") String authorizationHeader,
//                                                                   @PathVariable UUID companyID) {
//        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
//        var response = nineNineService.createUserCode(requesterID, companyID);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/register-authorize-user-code")
//    public ResponseEntity registerAuthorizeUserCode(@RequestHeader("Authorization") String authorizationHeader,
//                                                    @RequestBody ReceiveCustomerCodeToRegisterIFoodDTO dto) {
//        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
//        nineNineService.registerAuthorizeUserCode(requesterID, dto);
//
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/disconnect-ninenine-store/{companyID}")
//    public ResponseEntity disconnectNineNineStore(@RequestHeader("Authorization") String authorizationHeader,
//                                               @PathVariable UUID companyID) {
//        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
//        nineNineService.disconnectNineNineStore(requesterID, companyID);
//
//        return ResponseEntity.ok().build();
//    }
//}
