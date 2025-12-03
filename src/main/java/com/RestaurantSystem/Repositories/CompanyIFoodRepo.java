package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Company.CompanyIFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyIFoodRepo extends JpaRepository<CompanyIFood, UUID> {

}
