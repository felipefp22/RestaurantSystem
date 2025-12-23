package com.RestaurantSystem.Entities.Printer.DTOs;

import java.util.List;
import java.util.UUID;

public record DeletePrintSyncsDTO(
        UUID companyID,
        List<UUID> printSyncsToDeleteIDs
) {
}
