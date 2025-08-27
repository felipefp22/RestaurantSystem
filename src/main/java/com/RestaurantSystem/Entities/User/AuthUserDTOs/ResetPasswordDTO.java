package com.RestaurantSystem.Entities.User.AuthUserDTOs;

public record ResetPasswordDTO(
        String tokenID,
        String newPassword,
        String confirmPassword
) {
}
