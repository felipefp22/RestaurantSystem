package com.RestaurantSystem.Entities.User.AuthUserDTOs;

import jakarta.validation.constraints.NotEmpty;

public record AuthenticationDTO (
        String emailOrUsername,
        @NotEmpty String password
){
}
