package com.RestaurantSystem.Services.AuxsServices;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.EmployeePosition;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import org.springframework.stereotype.Service;

@Service
public class VerificationsServices {

    // <> ---------- Methods ---------- <>

    public boolean isOwnerOrManager(Company company, AuthUserLogin requester) {
        Boolean requesterHavePermission = false;

        if (company.getOwnerCompound().getOwner().equals(requester)) {
            requesterHavePermission = true;
        } else if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.MANAGER))) {
            requesterHavePermission = true;
        }

        return requesterHavePermission;
    }

    public boolean isOwnerOrManagerOrSupervisor(Company company, AuthUserLogin requester) {
        Boolean requesterHavePermission = false;

        if (company.getOwnerCompound().getOwner().equals(requester)) {
            requesterHavePermission = true;
        } else if (company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.MANAGER))
                || company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(requester) && e.getPosition().equals(EmployeePosition.SUPERVISOR))) {
            requesterHavePermission = true;
        }

        return requesterHavePermission;
    }


}
