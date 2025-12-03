package com.RestaurantSystem.Controllers.ThirdSuppliersControllers;

import com.RestaurantSystem.Services.AuxsServices.AzureServiceBusService;
import com.RestaurantSystem.Services.ThirdSuppliersService.ServiceBusThirdSuppliersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/servicebus-third-suppliers")
public class ServiceBusThirdSuppliersController {

    private final ServiceBusThirdSuppliersService serviceBusThirdSuppliersService;

    public ServiceBusThirdSuppliersController(ServiceBusThirdSuppliersService serviceBusThirdSuppliersService) {
        this.serviceBusThirdSuppliersService = serviceBusThirdSuppliersService;
    }

    // <> ------------- Methods ------------- <>
    @GetMapping("/test")
    public ResponseEntity testEndpoint() {

        serviceBusThirdSuppliersService.sendTestMessage();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-open-shifts-to-pooling")
    public ResponseEntity<Object> getOpenShiftsToPooling() {

        var response = serviceBusThirdSuppliersService.getOpenShiftsToPooling();
        return ResponseEntity.ok(response);
    }

}
