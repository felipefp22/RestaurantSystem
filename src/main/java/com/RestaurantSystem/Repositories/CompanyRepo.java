package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepo extends JpaRepository<Company, UUID> {

    Optional<Company> findByOwner(String requesterID);

    Optional<Company> findByCompanyName(String companyName);
}
