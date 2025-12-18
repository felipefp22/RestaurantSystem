package com.RestaurantSystem.Services.AuxsServices;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.EmployeePosition;
import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.OrderRepo;
import com.RestaurantSystem.Repositories.ShiftRepo;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class VerificationsServices {

    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final ShiftRepo shiftRepo;
    private final OrderRepo orderRepo;

    public VerificationsServices(AuthUserRepository authUserRepository, CompanyRepo companyRepo, ShiftRepo shiftRepo, OrderRepo orderRepo) {
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.shiftRepo = shiftRepo;
        this.orderRepo = orderRepo;
    }

    // <> ---------------------------------------- Methods ---------------------------------------- <>

    // <> --------- Basic Company Infos ---------- <>
    public AuthUserLogin retrieveRequester(String requesterID) {
        return authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
    }

    public Company retrieveCompany(UUID companyID) {
        return companyRepo.findById(companyID)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    public Shift retrieveCurrentShift(Company company) {
        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company).orElseThrow(() -> new RuntimeException("No active shift found"));

        if (openedShift.size() > 1) {
            return openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
        } else {
            return openedShift.get(0);
        }
    }

    public Order retrieveOrderOpenedDoesnoteMatterShift(Company company, UUID orderID) {
        //Thats why if order is from another shift that not current, its will find anyway
        List<Order> orderOpened = orderRepo.findByStatusInAndShift_Company(List.of(OrderStatus.OPEN, OrderStatus.CLOSEDWAITINGPAYMENT), company);
        return orderOpened.stream().filter(x -> x.getId().equals(orderID)).findFirst().orElseThrow(() -> new RuntimeException("Order not found on that company."));
    }

    // <> ---------- Specific Positions ---------- <>
    public boolean isOwner(Company company, AuthUserLogin user) {
        return company.getOwnerCompound().getOwner().equals(user);
    }

    public boolean isServer(Company company, AuthUserLogin user) {
        return company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(user) && e.getPosition().equals(EmployeePosition.SERVER));
    }

    public boolean isManager(Company company, AuthUserLogin user) {
        return company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(user) && e.getPosition().equals(EmployeePosition.MANAGER));
    }

    public boolean isSupervisor(Company company, AuthUserLogin user) {
        return company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(user) && e.getPosition().equals(EmployeePosition.SUPERVISOR));
    }

    public boolean isWaiter(Company company, AuthUserLogin user) {
        return company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(user) && e.getPosition().equals(EmployeePosition.WAITER));
    }

    public boolean isDeliveryman(Company company, AuthUserLogin user) {
        return company.getEmployees().stream().anyMatch(e -> e.getEmployee().equals(user) && e.getPosition().equals(EmployeePosition.DELIVERYMAN));
    }


    // <>---------- Authorizes If ---------- <>
    public void justOwner(Company company, AuthUserLogin user) {
        if (!isOwner(company, user)) {
            throw new RuntimeException("User is not owner of this company.");
        }
    }

    public void justOwnerOrManager(Company company, AuthUserLogin user) {
        if (!isOwner(company, user) && !isManager(company, user)) {
            throw new RuntimeException("User is not owner or manager of this company.");
        }
    }

    public void justOwnerOrManagerOrSupervisor(Company company, AuthUserLogin user) {
        if(!isOwner(company, user) && !isManager(company, user) && !isSupervisor(company, user)) {
            throw new RuntimeException("User is not owner, manager or supervisor of this company.");
        }
    }

    public void justOwnerOrManagerOrSupervisorOrServer(Company company, AuthUserLogin user) {
        if(!isOwner(company, user) && !isManager(company, user) && !isSupervisor(company, user) && !isServer(company, user)) {
            throw new RuntimeException("User is not owner, manager, supervisor or server of this company.");
        }
    }

    public void worksOnCompany(Company company, AuthUserLogin user) {
        if (!isOwner(company, user)
                && !isServer(company, user)
                && !isManager(company, user)
                && !isSupervisor(company, user)
                && !isWaiter(company, user)
                && !isDeliveryman(company, user)) {
            throw new RuntimeException("User does not work at this company.");
        }
    }
}
