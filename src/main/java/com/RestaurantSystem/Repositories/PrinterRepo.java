package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Printer.Printer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrinterRepo extends JpaRepository<Printer, UUID> {


}
