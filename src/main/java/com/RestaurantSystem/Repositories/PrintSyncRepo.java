package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Printer.PrintSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrintSyncRepo extends JpaRepository<PrintSync, UUID> {

}
