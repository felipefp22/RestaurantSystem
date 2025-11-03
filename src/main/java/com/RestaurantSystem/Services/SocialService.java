package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.User.AuthUserDTOs.SocialUserResume;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import org.springframework.stereotype.Service;

@Service
public class SocialService {
    private final AuthUserRepository authUserRepository;

    public SocialService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    // <>--------------- Methodos ---------------<>
    public SocialUserResume findUserByEmail(String email) {
        AuthUserLogin userFound =
                authUserRepository.findById(email).orElseThrow(() -> new IllegalArgumentException("userNotFound"));

        return new SocialUserResume(userFound);
    }
}
