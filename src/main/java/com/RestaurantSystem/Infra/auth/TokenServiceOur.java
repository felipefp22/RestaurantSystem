package com.RestaurantSystem.Infra.auth;

import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Entities.User.JWKS;
import com.RestaurantSystem.Entities.User.RefreshToken;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.RefreshTokenRepository;
import com.RestaurantSystem.Services.JwksService;
import com.RestaurantSystem.Services.Utils.PemUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class TokenServiceOur {

    @Value("${how.many.devices.can.be.logged}")
    private int howManyDevicesCanBeLogged;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwksService jwksService;

    public TokenServiceOur(RefreshTokenRepository refreshTokenRepository, JwksService jwksService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwksService = jwksService;
    }


    // <>--------------- Methods ---------------<>
    public String generateToken(AuthUserLogin authUserLogin){
        try{
            JWKS currentKey = jwksService.getLastGeneratedKey();

            RSAPrivateKey privateKey = PemUtils.parseRSAPrivateKey(currentKey.getPrivatePem());
            RSAPublicKey publicKey = PemUtils.parseRSAPublicKey(currentKey.getPublicPem());

            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);

            List<String> roles = authUserLogin.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = JWT.create()
                    .withIssuer("prev")
                    .withSubject(authUserLogin.getEmail())
                    .withClaim("username", authUserLogin.getUsername())
                    .withClaim("username", authUserLogin.getUsername())
                    .withClaim("isEmailConfirmed", authUserLogin.isEmailConfirmed())
                    .withClaim("isPhoneConfirmed", authUserLogin.isPhoneConfirmed())
                    .withClaim("phone", authUserLogin.getPhone())
                    .withArrayClaim("roles", roles.toArray(new String[0]))
                    .withKeyId(currentKey.getKid().toString())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);

            return token;

        }catch (JWTCreationException exception){
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public String validateTokenGetID(String token){
        try{
            Algorithm algorithm = decodeJwtAndCompareKidsGetAlgorithm(token);

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
            Algorithm algorithm = decodeJwtAndCompareKidsGetAlgorithm(token);

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

    public Algorithm decodeJwtAndCompareKidsGetAlgorithm(String token) {
        try {
            DecodedJWT decoded = JWT.decode(token);
            String kid = decoded.getKeyId();
            if (kid == null)  throw new RuntimeException("Token does not contain kid");

            JWKS matchingKey = jwksService.tryReadSignatureGetMatchingKey(kid);
            RSAPublicKey publicKey = PemUtils.parseRSAPublicKey(matchingKey.getPublicPem());

            return Algorithm.RSA256(publicKey, null);

        } catch (Exception e) {
            throw new RuntimeException("Error while validating token", e);
        }
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
