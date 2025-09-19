package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyEmployeesRepo extends JpaRepository<CompanyEmployees, UUID> {

}
