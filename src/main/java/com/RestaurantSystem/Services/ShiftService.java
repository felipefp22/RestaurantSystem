package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ShiftRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ShiftService {

    private final ShiftRepo shiftRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;

    public ShiftService(ShiftRepo shiftRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo) {
        this.shiftRepo = shiftRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
    }

    //<>------------ Methods ------------<>
    public List<Shift> getAllShifts(String requesterID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return company.getShifts();
    }

    public Shift createShift(String requesterID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product, ask to manager");

        company.getShifts().stream()
                .filter(s -> s.getEndTimeUTC() == null)
                .findFirst()
                .ifPresent(s -> {
                    throw new RuntimeException("There is already an active shift");
                });

        String shiftNumber = String.valueOf(company.getShifts().size() + 1);
        Shift shift = new Shift(company, shiftNumber);

        return shiftRepo.save(shift);
    }

    public Shift closeShift(String requesterID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product, ask to manager");

        Shift shift = company.getShifts().stream()
                .filter(s -> s.getEndTimeUTC() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active shift found"));

        shift.setEndTimeUTC(LocalDateTime.now(ZoneOffset.UTC));

        return shiftRepo.save(shift);
    }
}
