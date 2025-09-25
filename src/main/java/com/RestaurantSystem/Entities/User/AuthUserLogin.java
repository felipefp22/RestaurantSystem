package com.RestaurantSystem.Entities.User;


import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.ENUMs.Role;
import com.RestaurantSystem.Entities.User.AuthUserDTOs.RegisterAuthUserDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

@Entity
@Table(name = "auth_user_login")
public class AuthUserLogin implements UserDetails {
    @Id
    private String email;

    private String password;

    @Enumerated(EnumType.ORDINAL)
    private Role role;
    private Boolean emailConfirmed;

    private String name;
    private String phone;

    private Boolean phoneConfirmed;

    @Column(length = 512)
    private String urlProfilePhoto;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompaniesCompound> companiesCompounds;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompanyEmployees> worksAtCompanies;

    private String ownAdministrativePassword;

    // <>------------ Constructors ------------<>

    public AuthUserLogin() {
    }

    public AuthUserLogin(String email, String name, String username, String password, boolean emailConfirmed) {
        this.email = email;
        this.name = name;
//        this.username = username;
        this.password = new BCryptPasswordEncoder().encode(password);
//        this.kindOfUser = kindOfUser;
        this.companiesCompounds = new ArrayList<>();
        this.role = Role.USER;
        this.emailConfirmed = emailConfirmed;
        this.phoneConfirmed = false;
    }

    public AuthUserLogin(RegisterAuthUserDTO registerAuthUserDTO) {
        this.email = registerAuthUserDTO.email();
//        this.username = registerAuthUserDTO.username();
        this.password = new BCryptPasswordEncoder().encode(registerAuthUserDTO.password());
//        this.kindOfUser = KindOfUser.valueOf(registerAuthUserDTO.kindOfUser().toUpperCase());
        this.name = registerAuthUserDTO.name();
        this.companiesCompounds = new ArrayList<>();
        this.role = Role.USER;
        this.emailConfirmed = false;
        this.phoneConfirmed = false;
    }

    // <>------------ Getters and setters ------------<>


    public String getEmail() {
        return email;
    }

//    public String getEmail() {
//        return username;
//    }
//    public void setUsername(String username) {
//        this.username = username;
//    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == Role.MASTERADMIN)
            return List.of(new SimpleGrantedAuthority("ROLE_MASTERADMIN"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));


        if (this.role == Role.ADMIN)
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER"));

        if (this.role == Role.USER) return List.of(new SimpleGrantedAuthority("ROLE_USER"));


        return null;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return "";
    }

    public void setPassword(String password) {
        this.password = new BCryptPasswordEncoder().encode(password);
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void comfirmEmail() {
        this.emailConfirmed = true;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean isPhoneConfirmed() {
        return phoneConfirmed;
    }

    public String getUrlProfilePhoto() {
        return urlProfilePhoto;
    }
    public void setUrlProfilePhoto(String urlProfilePhoto) {
        this.urlProfilePhoto = urlProfilePhoto;
    }

    public List<CompaniesCompound> getCompaniesCompounds() {
        return companiesCompounds;
    }

    public List<CompanyEmployees> getWorksAtCompanies() {
        return worksAtCompanies;
    }

    public String getOwnAdministrativePassword() {
        return ownAdministrativePassword;
    }
    public void setOwnAdministrativePassword(String ownAdministrativePassword) {
        this.ownAdministrativePassword = new BCryptPasswordEncoder().encode(ownAdministrativePassword);
    }
}
