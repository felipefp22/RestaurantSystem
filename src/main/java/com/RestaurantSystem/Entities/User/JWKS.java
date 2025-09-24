package com.RestaurantSystem.Entities.User;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "AUTH_jwks")
public class JWKS {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID kid;

    @Column(length = 5000)
    private String privatePem;

    @Column(length = 2000)
    private String publicPem;

    private LocalDateTime createdAtUTC;

    // <> ------------ Constructors ------------ <>
    public JWKS() {
    }
    public JWKS(String privatePem, String publicPem) {
        this.privatePem = privatePem;
        this.publicPem = publicPem;
        this.createdAtUTC = LocalDateTime.now(ZoneOffset.UTC);
    }

    // <> ------------ Getters and Setters ------------ <>
    public UUID getKid() {
        return kid;
    }
    public String getPrivatePem() {
        return privatePem;
    }
    public String getPublicPem() {
        return publicPem;
    }
    public LocalDateTime getCreatedAtUTC() {
        return createdAtUTC;
    }
}
