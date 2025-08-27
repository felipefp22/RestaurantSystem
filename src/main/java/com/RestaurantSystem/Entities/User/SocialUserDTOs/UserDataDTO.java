package com.RestaurantSystem.Entities.User.SocialUserDTOs;


import com.RestaurantSystem.Entities.User.AuthUserLogin;

public record UserDataDTO(
        String email,
        String name,
        String phone,
        String urlProfilePhoto
) {
    public UserDataDTO(AuthUserLogin authUserLogin) {
        this(authUserLogin.getEmail(), authUserLogin.getName(), authUserLogin.getPhone(), authUserLogin.getUrlProfilePhoto());
    }
}
