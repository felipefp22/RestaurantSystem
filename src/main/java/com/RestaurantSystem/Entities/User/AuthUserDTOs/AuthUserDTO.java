package com.RestaurantSystem.Entities.User.AuthUserDTOs;

import com.RestaurantSystem.Entities.User.AuthUserLogin;

public record AuthUserDTO(
        String email
) {
    public AuthUserDTO(AuthUserLogin authUserLogin) {
        this(authUserLogin.getEmail());
    }

}
