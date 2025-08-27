package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.ENUMs.Role;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUserLogin, String> {

    Optional<AuthUserLogin> findByPhone(String phoneNumber);
    List<AuthUserLogin> findByEmailContainingIgnoreCase(String email);
    List<AuthUserLogin> findAllByRoleIn(List<Role> hiringadmin);
}
