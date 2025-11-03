package com.RestaurantSystem.Entities.User.AuthUserDTOs;

import com.RestaurantSystem.Entities.User.AuthUserLogin;

public record SocialUserResume(
        String email,
        String name,
        String urlProfilePhoto
) {
    public SocialUserResume(AuthUserLogin user) {
        this(
                user.getEmail(),
                user.getName(),
                user.getUrlProfilePhoto()
        );
    }

}
