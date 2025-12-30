package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Shift.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepo extends JpaRepository<Shift, String> {
    Optional<List<Shift>> findAllByCompany_IdAndEndTimeUTCIsNull(UUID company);

    Optional<List<Shift>> findAllByEndTimeUTCIsNull();
}
