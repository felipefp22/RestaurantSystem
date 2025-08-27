package com.RestaurantSystem.Infra.auth;

import org.springframework.stereotype.Service;

@Service
public class RetriveAuthInfosService {
    private final TokenServiceOur tokenServiceOur;

    public RetriveAuthInfosService(TokenServiceOur tokenServiceOur) {
        this.tokenServiceOur = tokenServiceOur;
    }


    // <>--------------- Methodos ---------------<>

    public String retrieveEmailOfUser(String token) {
        String userName = tokenServiceOur.validateTokenGetID(token.replace("Bearer ", ""));
        return userName;
    }
    public String retrieveUsernameOfUser(String token) {
        String userName = tokenServiceOur.validateTokenGetUsername(token.replace("Bearer ", ""));
        return userName;
    }
}
