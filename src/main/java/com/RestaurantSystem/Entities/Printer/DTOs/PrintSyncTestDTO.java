package com.RestaurantSystem.Entities.Printer.DTOs;

import java.util.UUID;

public record PrintSyncTestDTO(
        PrinterData printer,
        String data,
        UUID companyID,
        UUID orderID

) {
    public record PrinterData(
            //its printerType
            String type,
            String lastKnownIP,
            String mac
    ){
    }
}
