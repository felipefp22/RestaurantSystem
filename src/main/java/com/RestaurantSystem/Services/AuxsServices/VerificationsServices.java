package com.RestaurantSystem.Services.AuxsServices;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.EmployeePosition;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import org.springframework.stereotype.Service;

@Service
public class VerificationsServices {

    // <> ---------- Methods ---------- <>

    public boolean isOwner(Company company, AuthUserLogin user) {
        Boolean requesterHavePermission = false;

        if (company.getOwnerCompound().getOwner().equals(user)) {
            requesterHavePermission = true;
        }

        return requesterHavePermission;
    }

    public boolean isOwnerOrManager(Company company, AuthUserLogin user) {
        Boolean requesterHavePermission = false;

//        AuthUserLogin employeeFound = company.getEmployees().stream()
//                .filter(e -> e.getEmployee().equals(user))
//                .map(e -> e.getEmployee())
//                .findFirst()
//                .orElse(null);

        if (company.getOwnerCompound().getOwner().equals(user)) {
            requesterHavePermission = true;
        } else if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(user) && e.getPosition().equals(EmployeePosition.MANAGER ))) {
            requesterHavePermission = true;
        }

        return requesterHavePermission;
    }

    public boolean isOwnerOrManagerOrSupervisor(Company company, AuthUserLogin user) {
        Boolean requesterHavePermission = false;

        if (company.getOwnerCompound().getOwner().equals(user)) {
            requesterHavePermission = true;
        } else if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(user) && e.getPosition().equals(EmployeePosition.MANAGER))
                || company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(user) && e.getPosition().equals(EmployeePosition.SUPERVISOR))) {
            requesterHavePermission = true;
        }

        return requesterHavePermission;
    }

    public boolean worksOnCompany(Company company, AuthUserLogin user) {
        Boolean requesterHavePermission = false;

        if (company.getOwnerCompound().getOwner().equals(user)) {
            requesterHavePermission = true;
        } else if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(user))) {
            requesterHavePermission = true;
        }

        return requesterHavePermission;
    }


}
