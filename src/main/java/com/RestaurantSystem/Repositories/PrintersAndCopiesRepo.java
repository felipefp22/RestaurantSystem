package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Printer.PrintersAndCopies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrintersAndCopiesRepo extends JpaRepository<PrintersAndCopies, UUID> {


}
