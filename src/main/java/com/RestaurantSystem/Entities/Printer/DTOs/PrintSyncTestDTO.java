package com.RestaurantSystem.Entities.Printer.DTOs;

import com.RestaurantSystem.Entities.ENUMs.PrintCategory;

import java.util.UUID;

public record PrintSyncTestDTO(
        PrinterData printer,
        String data,
        UUID companyID,
        UUID orderID,
        PrintCategory printCategory

) {
    public record PrinterData(
            //its printerType
            String type,
            String lastKnownIP,
            String mac
    ){
    }
}
