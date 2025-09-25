package com.RestaurantSystem.Entities.User;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 1500)
    private String associatedToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email")
    private AuthUserLogin user;

    private Instant expiryDate;


    // <> ------------ Constructors ------------ <>
    public RefreshToken() {
    }
    public RefreshToken(AuthUserLogin user, String token , Instant refreshTokenExpirationDate) {
        this.user = user;
        this.associatedToken = token;
        this.expiryDate = refreshTokenExpirationDate;
    }


    // <> ------------ Getters and Setters ------------ <>

    public UUID getId() {
        return id;
    }

    public String getAssociatedToken() {
        return associatedToken;
    }

    public AuthUserLogin getUser() {
        return user;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }
}