package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Printer.PrintSync;
import com.RestaurantSystem.Entities.Shift.DTOs.CloseShiftDTO;
import com.RestaurantSystem.Entities.Shift.DTOs.ShiftOperationDTO;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.PrintSyncRepo;
import com.RestaurantSystem.Repositories.ShiftRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ShiftService {

    private final ShiftRepo shiftRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final VerificationsServices verificationsServices;
    private final PrintSyncRepo printSyncRepo;

    public ShiftService(ShiftRepo shiftRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo, VerificationsServices verificationsServices, PrintSyncRepo printSyncRepo) {
        this.shiftRepo = shiftRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.verificationsServices = verificationsServices;
        this.printSyncRepo = printSyncRepo;
    }

    //<>------------ Methods ------------<>
    public ShiftOperationDTO getShiftOperationRequesterAlreadyVerified(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.worksOnCompany(company, requester);

        return getShiftOperationRequesterAlreadyVerified(company);
    }

    public Set<Shift> getAllShifts(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        return company.getShifts();
    }

    @Transactional
    public Shift createShift(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);

        company.getShifts().stream()
                .filter(s -> s.getEndTimeUTC() == null)
                .findFirst()
                .ifPresent(s -> {
                    throw new RuntimeException("There is already an active shift");
                });

        String shiftNumber = company.getLastShiftNumber() == null ? "1" : String.valueOf(Integer.valueOf(company.getLastShiftNumber()) + 1);
        Shift shift = new Shift(company, shiftNumber, requester);

        Shift shiftSaved = shiftRepo.save(shift);
        company.setLastOrOpenShift(shiftSaved);
        company.setLastShiftNumber(shiftSaved.getShiftNumber());
        company.getShifts().add(shiftSaved);
        companyRepo.save(company);

        return shiftSaved;
    }

    public Shift closeShift(String requesterID, CloseShiftDTO closeShiftDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(closeShiftDTO.companyID());
        verificationsServices.justOwnerOrManager(company, requester);


        if (new BCryptPasswordEncoder().matches(closeShiftDTO.adminPassword(), requester.getOwnAdministrativePassword())) {
            Shift shift = company.getShifts().stream()
                    .filter(s -> s.getEndTimeUTC() == null)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No active shift found"));

            if (!shift.getId().equals(closeShiftDTO.shiftID())) throw new RuntimeException("invalidShiftID");

            shift.setEndTimeUTC(LocalDateTime.now(ZoneOffset.UTC));
            shift.setEmployeeClosedShift(requester.getEmail());

            Set<PrintSync> pintSyncsToDelete = company.getPrintSync();
            printSyncRepo.deleteAll(pintSyncsToDelete);
            return shiftRepo.save(shift);
        } else {
            throw new RuntimeException("invalidAdminPassword");
        }
    }

    // <>------------ Split Methods ------------<>
    public ShiftOperationDTO getShiftOperationRequesterAlreadyVerified(Company company) {
        Shift currentShift = verificationsServices.retrieveCurrentShift(company);

        Shift previousShift = shiftRepo.findById(company.getId().toString() + "_" + (Integer.parseInt(currentShift.getShiftNumber()) - 1))
                .orElse(null);

        List<Order> previousShiftStillOpenOrders = previousShift == null ? List.of() : previousShift.getOrders().stream()
                .filter(o -> o.getCompletedOrderDateUtc() == null)
                .toList();

        List<Order> ordersOnOperation = currentShift.getOrders();
        ordersOnOperation.addAll(previousShiftStillOpenOrders);

        return new ShiftOperationDTO(currentShift, ordersOnOperation, company.getPrintSync());
    }
}
