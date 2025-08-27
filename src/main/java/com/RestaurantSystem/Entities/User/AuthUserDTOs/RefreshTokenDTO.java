package com.RestaurantSystem.Entities.User.AuthUserDTOs;

import java.util.UUID;

public record RefreshTokenDTO(
        UUID refreshToken,
        String associatedToken,
        String fcmToken
) {
}