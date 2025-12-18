package com.RestaurantSystem.Entities.Printer.DTOs;

import java.util.UUID;

public record CreateOrUpdatePrinterDTO(
        UUID companyID,
        UUID printerID,
        String printerCustomName,
        String type,
        String name,
        String usbName,
        String mac,
        String ip
) {
}
