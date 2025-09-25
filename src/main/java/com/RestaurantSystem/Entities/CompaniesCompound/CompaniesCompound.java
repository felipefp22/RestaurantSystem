package com.RestaurantSystem.Entities.CompaniesCompound;

import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CreateOrUpdateCompoundDTO;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class CompaniesCompound {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_email")
    private AuthUserLogin owner;

    private String compoundName;
    private String compoundDescription;

    @OneToMany(mappedBy = "ownerCompound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Company> companies;

    // <>------------ Constructors ------------<>
    private CompaniesCompound() {
    }

    public CompaniesCompound(AuthUserLogin owner, CreateOrUpdateCompoundDTO createOrUpdateCompoundDTO) {
        this.owner = owner;
        this.compoundName = createOrUpdateCompoundDTO.compoundName();
        this.compoundDescription = createOrUpdateCompoundDTO.compoundDescription();
        this.companies = new ArrayList<>();;
    }

    // <>------------ Getters and Setters ------------<>

    public UUID getId() {
        return id;
    }

    public AuthUserLogin getOwner() {
        return owner;
    }

    public String getCompoundName() {
        return compoundName;
    }

    public void setCompoundName(String compoundName) {
        this.compoundName = compoundName;
    }

    public String getCompoundDescription() {
        return compoundDescription;
    }

    public void setCompoundDescription(String compoundDescription) {
        this.compoundDescription = compoundDescription;
    }

    public void setOwner(AuthUserLogin owner) {
        this.owner = owner;
    }

    public List<Company> getCompanies() {
        return companies;
    }
}
