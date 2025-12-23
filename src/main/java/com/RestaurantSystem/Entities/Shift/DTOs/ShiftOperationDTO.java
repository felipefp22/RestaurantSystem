package com.RestaurantSystem.Entities.Shift.DTOs;

import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Printer.PrintSync;
import com.RestaurantSystem.Entities.Shift.Shift;

import java.util.List;

public record ShiftOperationDTO(
        Shift currentShift,
        List<Order> orders,
        List<PrintSync> printSync
) {
}
