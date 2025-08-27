package com.RestaurantSystem.Entities.User.AuthUserDTOs;

import com.RestaurantSystem.Entities.User.AuthUserLogin;

public record UserDataAdminsDTO(
        String email,
        String name,
        String phone,
        String urlProfilePhoto,
        String role
) {
    public UserDataAdminsDTO(AuthUserLogin authUserLogin) {
        this(authUserLogin.getEmail(), authUserLogin.getName(), authUserLogin.getPhone(), authUserLogin.getUrlProfilePhoto(), authUserLogin.getRole().toString());
    }
}
