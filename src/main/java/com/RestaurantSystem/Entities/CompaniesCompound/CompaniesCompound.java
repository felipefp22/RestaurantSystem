package com.RestaurantSystem.Entities.CompaniesCompound;

import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CreateOrUpdateCompoundDTO;
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
    private String compoundName;
    private String compoundDescription;

    @OneToMany
    private List<Company> companies;

    // <>------------ Constructors ------------<>
    private CompaniesCompound() {
    }

    public CompaniesCompound(AuthUserLogin owner, CreateOrUpdateCompoundDTO createOrUpdateCompoundDTO) {
        this.owner = owner;
        this.compoundName = createOrUpdateCompoundDTO.compoundName();
        this.compoundDescription = createOrUpdateCompoundDTO.compoundDescription();
        this.companies = List.of();
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
