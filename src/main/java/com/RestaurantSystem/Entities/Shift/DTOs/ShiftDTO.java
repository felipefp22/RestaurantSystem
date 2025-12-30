package com.RestaurantSystem.Entities.Shift.DTOs;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Shift.Shift;

import java.time.LocalDateTime;
import java.util.List;

public record ShiftDTO(
        String id,
        String shiftNumber,
        LocalDateTime startTimeUTC,
        LocalDateTime endTimeUTC,
//        List<Order> orders,
        String employeeOpenedShift,
        String employeeClosedShift
) {
    public ShiftDTO(Shift shift){
        this(
                shift.getId(),
                shift.getShiftNumber(),
                shift.getStartTimeUTC(),
                shift.getEndTimeUTC(),
//                shift.getOrders(),
                shift.getManagerWhoseOpenedShift(),
                shift.getEmployeeClosedShift()
        );
    }
}
