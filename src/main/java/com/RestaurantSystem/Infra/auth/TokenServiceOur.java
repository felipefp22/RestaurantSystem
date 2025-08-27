package com.RestaurantSystem.Infra.auth;

import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Entities.User.RefreshToken;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class TokenServiceOur {
    @Value("${oauth.jwt.secret}")
    private String secretKey;

    @Value("${how.many.devices.can.be.logged}")
    private int howManyDevicesCanBeLogged;

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenServiceOur(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }


    // <>--------------- Methods ---------------<>
    public String generateToken(AuthUserLogin authUserLogin){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            String token = JWT.create()
                    .withIssuer("prev")
                    .withSubject(authUserLogin.getEmail())
                    .withClaim("username", authUserLogin.getUsername())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);

            return token;

        }catch (JWTCreationException exception){
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public String validateTokenGetID(String token){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            return JWT.require(algorithm)
                    .withIssuer("prev")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTCreationException exception){
            return "";
        }
    }

    public String validateTokenGetUsername(String token){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            return JWT.require(algorithm)
                    .withIssuer("prev")
                    .build()
                    .verify(token)
                    .getClaim("username").asString();
        } catch (JWTCreationException exception){
            return "";
        }
    }

    private Instant genExpirationDate(){
        return LocalDateTime.now(ZoneOffset.UTC)
                .plusMinutes(10)
                .toInstant(ZoneOffset.UTC);
    }

    public Instant genRefreshTokenExpirationDate(){
        return LocalDateTime.now(ZoneOffset.UTC)
                .plusDays(30)
                .toInstant(ZoneOffset.UTC);
    }

    @Transactional
    public RefreshToken createRefreshToken(AuthUserLogin user, String token) {
        List<RefreshToken> userRefreshTokens = refreshTokenRepository.findByUser(user).orElse(null);

        if (userRefreshTokens != null && userRefreshTokens.size() >= howManyDevicesCanBeLogged) {
            userRefreshTokens.sort((x, y) -> x.getExpiryDate().compareTo(y.getExpiryDate()));
            refreshTokenRepository.delete(userRefreshTokens.get(0));
        }

        RefreshToken refreshToken = new RefreshToken(user, token, genRefreshTokenExpirationDate());

        return refreshTokenRepository.save(refreshToken);
    }

    public void deleteRefreshToken(UUID token) {
        refreshTokenRepository.deleteById(token);
    }

    public RefreshToken findRefreshTokenByToken(UUID token) {
        return refreshTokenRepository.findById(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }

    @Scheduled(fixedRate = 21600000)
    public void deleteRefreshTokensNoLongerValid() {
        System.out.println("------>> Deleting expired refresh tokens");
        refreshTokenRepository.findAll().forEach(x -> {
            if (x.getExpiryDate().isBefore(Instant.now())) {
                refreshTokenRepository.delete(x);
            }
        });
    }
}
