package com.RestaurantSystem.Entities.Printer.DTOs;

import com.RestaurantSystem.Entities.ENUMs.PrintCategory;

import java.util.UUID;

public record UpdatePrintRulesDTO(
        UUID companyID,
        PrintCategory printCategory,
        UUID printAndCopiesID,
        UUID printerID,
        Integer copies

){
}
