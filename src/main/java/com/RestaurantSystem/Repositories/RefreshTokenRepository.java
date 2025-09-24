package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Entities.User.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<List<RefreshToken>> findByUser(AuthUserLogin user);

    void deleteAllByUser(AuthUserLogin userToDel);
}
