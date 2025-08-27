package com.RestaurantSystem.Entities.User.AuthUserDTOs;

import jakarta.validation.constraints.*;

public record RegisterAuthUserDTO(
        @NotBlank @NotEmpty @Size(min = 4, message = "Password must at least 4 digits") String name,
        @NotBlank @NotEmpty @Email String email,
        @NotBlank @NotEmpty @Size(min = 6, message = "Password must at least 6 digits") String password
) {

//    public RegisterAuthUserDTO(TableEntity entity) {
//        this(entity.getRowKey().toString(),
//                entity.getPartitionKey().toString(),
//                entity.getProperty("password").toString(),
//                "USER");
//
//    }
}
