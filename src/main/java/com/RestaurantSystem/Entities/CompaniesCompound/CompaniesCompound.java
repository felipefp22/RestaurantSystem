package com.RestaurantSystem.Entities.CompaniesCompound;

import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CreateOrUpdateCompoundDTO;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.*;

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

    @OneToMany(mappedBy = "ownerCompound", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Company> companies;

    // <>------------ Constructors ------------<>
    private CompaniesCompound() {
    }

    public CompaniesCompound(AuthUserLogin owner, CreateOrUpdateCompoundDTO createOrUpdateCompoundDTO) {
        this.owner = owner;
        this.compoundName = createOrUpdateCompoundDTO.compoundName();
        this.compoundDescription = createOrUpdateCompoundDTO.compoundDescription();
        this.companies = new HashSet<>();;
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

    public Set<Company> getCompanies() {
        return companies;
    }
}
