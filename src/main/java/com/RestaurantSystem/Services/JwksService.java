package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.User.JWKS;
import com.RestaurantSystem.Repositories.JwksRepository;
import com.RestaurantSystem.Services.Utils.Base64Url;
import com.RestaurantSystem.Services.Utils.PemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwksService {

    @Value("${minutes.duration.token}")
    private int minutesDurationToken;

    private final JwksRepository jwksRepository;

    public JWKS lastGeneratedKey;
    public List<JWKS> allKeysInMemory;

    public JwksService(JwksRepository jwksRepository) {
        this.jwksRepository = jwksRepository;
    }

    // <> ------------ Methods ------------ <>
    public Map<String, Object> jwks() {
        List<JWKS> keys = deleteOldKeysAndCreateNewIfNeededAndGetKeysList();

        List<Map<String, String>> keysJson = keys.stream().map(k -> {
            RSAPublicKey pub = PemUtils.parseRSAPublicKey(k.getPublicPem());
            String n = Base64Url.encodeUnsigned(pub.getModulus());
            String e = Base64Url.encodeUnsigned(pub.getPublicExponent());
            return Map.of(
                    "kty", "RSA", "alg", "RS256", "use", "sig",
                    "kid", k.getKid().toString(), "n", n, "e", e
            );
        }).collect(Collectors.toList());

        return Map.of("keys", keysJson);
    }

    public JWKS getLastGeneratedKey() {
        if (lastGeneratedKey == null) {
            deleteOldKeysAndCreateNewIfNeededAndGetKeysList();
        }

        return lastGeneratedKey;
    }

    public JWKS tryReadSignatureGetMatchingKey(String tokenIncomeKid) {
        if (allKeysInMemory == null) {
            deleteOldKeysAndCreateNewIfNeededAndGetKeysList();
        }

        return allKeysInMemory.stream()
                .filter(k -> k.getKid().toString().equals(tokenIncomeKid))
                .findFirst()
                .or(() -> {
                    deleteOldKeysAndCreateNewIfNeededAndGetKeysList();
                    return allKeysInMemory.stream()
                            .filter(k -> k.getKid().toString().equals(tokenIncomeKid))
                            .findFirst();
                })
                .orElseThrow(() -> new RuntimeException("No matching key found for kid=" + tokenIncomeKid));
    }

    //<> --------------- Helpers ----------- <>
    @Scheduled(fixedRate = 60000)
    private List<JWKS> deleteOldKeysAndCreateNewIfNeededAndGetKeysList() {
        List<JWKS> keys = jwksRepository.findAll(); // returns public_pem and kid
        int rotationKeyRate = minutesDurationToken * 10;

        keys = keys.stream()
                .sorted((k1, k2) -> k2.getCreatedAtUTC().compareTo(k1.getCreatedAtUTC()))
                .collect(Collectors.toList());

        if (keys.size() > 2) {
            keys.stream().skip(2).forEach(k -> {
                if (LocalDateTime.now(ZoneOffset.UTC).isAfter(k.getCreatedAtUTC().plusMinutes(rotationKeyRate + (minutesDurationToken * 2)))) {
                    jwksRepository.delete(k);
                }
            });
        }

        if (keys.isEmpty() || LocalDateTime.now(ZoneOffset.UTC).isAfter(keys.get(0).getCreatedAtUTC().plusMinutes(rotationKeyRate))) {
            JWKS newKey = generateNewKey();
            keys.add(newKey);
        } else {
            lastGeneratedKey = keys.get(0);
        }

        keys = keys.stream()
                .sorted((k1, k2) -> k2.getCreatedAtUTC().compareTo(k1.getCreatedAtUTC()))
                .collect(Collectors.toList());

        allKeysInMemory = keys;

        return keys;
    }

    public JWKS generateNewKey() {
        try {
            var keyPairGen = java.security.KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            var keyPair = keyPairGen.generateKeyPair();

            String privatePem = PemUtils.toPem(keyPair.getPrivate(), "PRIVATE KEY");
            String publicPem = PemUtils.toPem(keyPair.getPublic(), "PUBLIC KEY");

            JWKS newKey = new JWKS(privatePem, publicPem);
            jwksRepository.save(newKey);
            lastGeneratedKey = newKey;

            return newKey;
        } catch (Exception e) {
            throw new RuntimeException("Error generating RSA key pair", e);
        }
    }
}
