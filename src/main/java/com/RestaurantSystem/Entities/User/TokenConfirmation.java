package com.RestaurantSystem.Entities.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class TokenConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String action;
    private String userToChangeID;
    private LocalDateTime expirationDate;
    private int confirmationCode;
    private String auxData;

    // <>--------------- Constructors ---------------<>
    public TokenConfirmation() {
    }
    public TokenConfirmation(String action, String userToChangeID, LocalDateTime expirationDate, String auxData) {
        this.action = action;
        this.userToChangeID = userToChangeID;
        this.expirationDate = expirationDate;
        this.confirmationCode = 100000 + (int) (Math.random() * 900000);
        this.auxData = auxData;
    }


    // <>--------------- Getters and Setters ---------------<>

    public UUID getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getUserToChangeID() {
        return userToChangeID;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public int getConfirmationCode() {
        return confirmationCode;
    }

    public String getAuxData() {
        return auxData;
    }
}
