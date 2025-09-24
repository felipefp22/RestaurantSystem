//package com.RestaurantSystem.Controllers;//package com.Hiring.Controllers;
//
//import com.RestaurantSystem.Entities.User.JWKS;
//import com.RestaurantSystem.Infra.auth.TokenServiceOur;
//import com.RestaurantSystem.Services.JwksService;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//@RestController
//public class JwksController {
//
//    private final TokenServiceOur tokenService;
//    private final JwksService jwksService;
//
//    public JwksController(TokenServiceOur tokenService, JwksService jwksService) {
//        this.tokenService = tokenService;
//        this.jwksService = jwksService;
//    }
//
//    // <> ------------ Methods ------------ <>
//
//    @GetMapping("/there-keys/jwks.json")
//    public Map<String, Object> getKeys() {
//
//        return jwksService.jwks();
//    }
//
//    @PostMapping("/there-keys/generate-new-key")
//    public JWKS generateNewKey() {
//
//        return jwksService.generateNewKey();
//    }
//}