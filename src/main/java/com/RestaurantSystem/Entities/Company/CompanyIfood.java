package com.RestaurantSystem.Entities.Company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class CompanyIfood {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @OneToOne(mappedBy = "companyIfoodData")
    private Company company;

    // <>- Temporary Codes -<>
    private String lastGeneratedUserCode;
    private String lastGeneratedAuthorizationCodeVerifier;
    private String lastGeneratedFriendlyUrlUserCode;

    // <>- Store In Use Codes -<>
    private String storeCode;
    private String storeAuthorizationCodeVerifier;
    private String merchantID;
    private String merchantName;
    private String corporateName;

    @Column(length = 8500)
    private String accessToken;

    @Column(length = 8500)
    private String refreshToken;

    // <>------------ Constructors ------------<>
    public CompanyIfood() {
    }
    public CompanyIfood(Company company) {
        this.company = company;
    }


    // <>------------ Getters and Setters ------------<>

    public UUID getId() {
        return id;
    }
    public Company getCompany() {
        return company;
    }

    public String getLastGeneratedUserCode() {
        return lastGeneratedUserCode;
    }

    public void setLastGeneratedUserCode(String lastGeneratedUserCode) {
        this.lastGeneratedUserCode = lastGeneratedUserCode;
    }

    public String getLastGeneratedAuthorizationCodeVerifier() {
        return lastGeneratedAuthorizationCodeVerifier;
    }

    public void setLastGeneratedAuthorizationCodeVerifier(String lastGeneratedAuthorizationCodeVerifier) {
        this.lastGeneratedAuthorizationCodeVerifier = lastGeneratedAuthorizationCodeVerifier;
    }

    public String getLastGeneratedFriendlyUrlUserCode() {
        return lastGeneratedFriendlyUrlUserCode;
    }

    public void setLastGeneratedFriendlyUrlUserCode(String lastGeneratedFriendlyUrlUserCode) {
        this.lastGeneratedFriendlyUrlUserCode = lastGeneratedFriendlyUrlUserCode;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getStoreAuthorizationCodeVerifier() {
        return storeAuthorizationCodeVerifier;
    }

    public void setStoreAuthorizationCodeVerifier(String storeAuthorizationCodeVerifier) {
        this.storeAuthorizationCodeVerifier = storeAuthorizationCodeVerifier;
    }

    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(String corporateName) {
        this.corporateName = corporateName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
