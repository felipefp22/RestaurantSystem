package com.RestaurantSystem.Services.AuxsServices;

import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.implementation.core.annotation.ServiceBusListener;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class AzureServiceBusService {

    private final ServiceBusTemplate serviceBusTemplate;

    public AzureServiceBusService(ServiceBusTemplate serviceBusTemplate) {
        this.serviceBusTemplate = serviceBusTemplate;
        this.serviceBusTemplate.setDefaultEntityType(ServiceBusEntityType.QUEUE);
    }


    // <>--------------- Methods ---------------<>
    public void sendMessageToQueue(String queueName, String message) {
        serviceBusTemplate.sendAsync(queueName,
                        MessageBuilder.withPayload(message).build())
                .subscribe();
    }

    @ServiceBusListener(destination = "ThirdSuppliersPooling")
    public void thirdSuppliersPooling(String messageBody) {
        System.out.println("Received message: " + messageBody);
    }
}
