package com.RestaurantSystem.Entities.Company.DTOs;

import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.Company.EmployeePosition;
import com.RestaurantSystem.Entities.Company.EmployeeStatus;

import java.util.UUID;

public record CompanyEmployeesDTO(
        UUID companyID,
        String companyName,
        String employeeEmail,
        String employeeName,
        EmployeePosition position,
        EmployeeStatus status
) {
    public CompanyEmployeesDTO(CompanyEmployees ce) {
        this(
                ce.getCompany().getId(),
                ce.getCompany().getCompanyName(),
                ce.getEmployee().getEmail(),
                ce.getEmployee().getName(),
                ce.getPosition(),
                ce.getStatus()
        );
    }
}