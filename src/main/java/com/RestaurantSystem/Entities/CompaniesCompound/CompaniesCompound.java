package com.RestaurantSystem.Entities.CompaniesCompound;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
public class CompaniesCompound {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    private AuthUserLogin owner;

    @OneToMany
    private List<Company> companies;

    // <>------------ Constructors ------------<>
    private CompaniesCompound() {
    }

    public CompaniesCompound(AuthUserLogin owner, List<Company> companies) {
        this.owner = owner;
        this.companies = companies;
    }

    // <>------------ Getters and Setters ------------<>

    public UUID getId() {
        return id;
    }

    public AuthUserLogin getOwner() {
        return owner;
    }

    public void setOwner(AuthUserLogin owner) {
        this.owner = owner;
    }

    public List<Company> getCompanies() {
        return companies;
    }
}
