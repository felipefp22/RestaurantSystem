package com.RestaurantSystem.Entities.Company.DTOs;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.Company.EmployeePosition;
import com.RestaurantSystem.Entities.Company.EmployeeStatus;
import com.RestaurantSystem.Entities.Shift.DTOs.ShiftOperationDTO;
import com.RestaurantSystem.Entities.Shift.Shift;

import java.util.UUID;

public record CompanyEmployeesDTO(
        UUID id,
        String companyName,
        String employeeEmail,
        String employeeName,
        EmployeePosition position,
        EmployeeStatus status,
        Shift lastOrOpenShift
) {
    public CompanyEmployeesDTO(CompanyEmployees ce) {
        this(
                ce.getCompany().getId(),
                ce.getCompany().getCompanyName(),
                ce.getEmployee().getEmail(),
                ce.getEmployee().getName(),
                ce.getPosition(),
                ce.getStatus(),
                ce.getCompany().getLastOrOpenShift()
        );
    }

}