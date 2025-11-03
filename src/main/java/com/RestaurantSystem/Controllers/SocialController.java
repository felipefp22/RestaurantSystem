package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.User.AuthUserDTOs.SocialUserResume;
import com.RestaurantSystem.Services.SocialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/social")
public class SocialController {
    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    // <>--------------- Methodos ---------------<>
    @GetMapping("/get-user-by-email/{email}")
    public ResponseEntity<SocialUserResume> getUserByEmail(@PathVariable String email) {

        var response =  socialService.findUserByEmail(email);

        return ResponseEntity.ok(response);
    }
}
