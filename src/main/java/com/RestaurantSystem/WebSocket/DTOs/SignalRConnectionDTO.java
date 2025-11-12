package com.RestaurantSystem.WebSocket.DTOs;

public record SignalRConnectionDTO(
    String url,
    String accessToken,
    Claims claims
) {

    public record Claims(
        String group
    ) {

    }
}
