package com.RestaurantSystem.Entities.Printer.DTOs;

import java.util.UUID;

public record DeletePrinterDTO(
        UUID companyID,
        UUID printerID
) {
}
