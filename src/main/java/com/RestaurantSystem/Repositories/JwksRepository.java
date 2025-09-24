package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.User.JWKS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JwksRepository extends JpaRepository<JWKS, UUID> {

}
