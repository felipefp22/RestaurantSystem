package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Printer.PrintRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrintRulesRepo extends JpaRepository<PrintRules, UUID> {


}
