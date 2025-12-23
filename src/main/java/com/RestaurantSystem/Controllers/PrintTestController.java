package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Printer.DTOs.PrintSyncTestDTO;
import com.RestaurantSystem.Entities.Printer.PrintSync;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.PrintSyncRepo;
import com.RestaurantSystem.Services.AuxsServices.PrintSyncService;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/print-test")
public class PrintTestController {

    private final PrintSyncService printSyncService;
    private final VerificationsServices verificationsServices;
    private final RetriveAuthInfosService retriveAuthInfosService;
    private final PrintSyncRepo printSyncRepo;

    public PrintTestController(PrintSyncService printSyncService, VerificationsServices verificationsServices, RetriveAuthInfosService retriveAuthInfosService, PrintSyncRepo printSyncRepo) {
        this.printSyncService = printSyncService;
        this.verificationsServices = verificationsServices;
        this.retriveAuthInfosService = retriveAuthInfosService;
        this.printSyncRepo = printSyncRepo;
    }

    // <>------------ Methods ------------<>

    @PostMapping("/delivery")
    private void testDeliveryPrint(@RequestBody PrintSyncTestDTO dto,
                                   @RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.worksOnCompany(company, requester);
        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, dto.orderID());

        String printerLocalServerUrl = "http://localhost:27124/print";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PrintSyncTestDTO> request =
                new HttpEntity<>(new PrintSyncTestDTO(
                        new PrintSyncTestDTO.PrinterData(dto.printer().type(), dto.printer().lastKnownIP(), dto.printer().mac()), printSyncService.createDeliveryPrint(company, order), null,null, null), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                printerLocalServerUrl,
                HttpMethod.POST,
                request,
                String.class);

        System.out.println(response.getStatusCode());
    }

    @PostMapping("/test-print-sync")
    private void testPrintSyncPrint(@RequestBody PrintSyncTestDTO dto,
                                    @RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.worksOnCompany(company, requester);
        Order order = verificationsServices.retrieveOrderOpenedDoesnoteMatterShift(company, dto.orderID());

        String textTest = printSyncService.createDeliveryPrint(company, order);
        PrintSync printSync = new PrintSync(company, dto.printCategory(), textTest);

        printSyncRepo.save(printSync);
    }
}
