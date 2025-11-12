package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Shift.DTOs.CloseShiftDTO;
import com.RestaurantSystem.Entities.Shift.DTOs.ShiftOperationDTO;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ShiftRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ShiftService {

    private final ShiftRepo shiftRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final VerificationsServices verificationsServices;

    public ShiftService(ShiftRepo shiftRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo, VerificationsServices verificationsServices) {
        this.shiftRepo = shiftRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.verificationsServices = verificationsServices;
    }

    //<>------------ Methods ------------<>
    public ShiftOperationDTO getShiftOperationRequesterAlreadyVerified(String requesterID, String companyID){
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(companyID))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the shifts of this company");

        return getShiftOperationRequesterAlreadyVerified(company);
    }

    public List<Shift> getAllShifts(String requesterID, String companyID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(companyID))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, requester)) throw new RuntimeException("You are not allowed to see the shifts of this company");

        return company.getShifts();
    }

    public Shift createShift(String requesterID, String companyID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(companyID))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManager(company, requester)) throw new RuntimeException("You are not allowed to add a product, ask to manager");

        company.getShifts().stream()
                .filter(s -> s.getEndTimeUTC() == null)
                .findFirst()
                .ifPresent(s -> {
                    throw new RuntimeException("There is already an active shift");
                });

        String shiftNumber = String.valueOf(company.getShifts().size() + 1);
        Shift shift = new Shift(company, shiftNumber, requester);

        Shift shiftSaved = shiftRepo.save(shift);
        company.setLastOrOpenShift(shift);
        companyRepo.save(company);

        return shiftSaved;
    }

    public Shift closeShift(String requesterID, CloseShiftDTO closeShiftDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(closeShiftDTO.companyID()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManager(company, requester)) throw new RuntimeException("justOwnerOrManagerCanCloseShift");

        if (new BCryptPasswordEncoder().matches(closeShiftDTO.adminPassword(), requester.getOwnAdministrativePassword())) {
            Shift shift = company.getShifts().stream()
                    .filter(s -> s.getEndTimeUTC() == null)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No active shift found"));

            if (!shift.getId().equals(closeShiftDTO.shiftID())) throw new RuntimeException("invalidShiftID");

            shift.setEndTimeUTC(LocalDateTime.now(ZoneOffset.UTC));
            shift.setEmployeeClosedShift(requester.getEmail());

            return shiftRepo.save(shift);
        }else {
            throw new RuntimeException("invalidAdminPassword");
        }
    }

    // <>------------ Split Methods ------------<>
    public ShiftOperationDTO getShiftOperationRequesterAlreadyVerified(Company company){
        List<Shift> openedShift = shiftRepo.findAllByCompanyAndEndTimeUTCIsNull(company);
        if(openedShift.isEmpty()){
            return null;
        }
        Shift currentShift = null;
        if(openedShift.size() > 1){
            Shift lastShift = openedShift.stream()
                    .max(Comparator.comparing(Shift::getStartTimeUTC))
                    .orElse(null);
        } else {
            currentShift = openedShift.get(0);
        }
        Shift previousShift = shiftRepo.findById(company.getId().toString() + "_" + (Integer.parseInt(openedShift.get(0).getShiftNumber()) - 1))
                .orElse(null);

        List<Order> previousShiftStillOpenOrders = previousShift == null ? List.of() : previousShift.getOrders().stream()
                .filter(o -> o.getCompletedOrderDateUtc() == null)
                .toList();

        List<Order> ordersOnOperation = currentShift.getOrders();
        ordersOnOperation.addAll(previousShiftStillOpenOrders);

        return new ShiftOperationDTO(currentShift, ordersOnOperation);
    }

}
