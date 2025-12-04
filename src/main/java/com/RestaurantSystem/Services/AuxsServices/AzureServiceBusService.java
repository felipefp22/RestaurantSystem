package com.RestaurantSystem.Services.AuxsServices;

import com.RestaurantSystem.Entities.Company.DTOs.CompanyThirdSuppliersToPoolingDTO;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.implementation.core.annotation.ServiceBusListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class AzureServiceBusService {
    @Value("${spring.cloud.azure.servicebus.connection-string}")
    private String connectionString;

    private final ServiceBusTemplate serviceBusTemplate;
    ObjectMapper objectMapper = new ObjectMapper();

    public AzureServiceBusService(ServiceBusTemplate serviceBusTemplate) {
        this.serviceBusTemplate = serviceBusTemplate;
        this.serviceBusTemplate.setDefaultEntityType(ServiceBusEntityType.QUEUE);
    }


    // <>--------------- Methods ---------------<>
    public void sendMessageToQueue(String queueName, Object message) {
        serviceBusTemplate.sendAsync(queueName,
                        MessageBuilder.withPayload(message).build())
                .subscribe();
    }

    @ServiceBusListener(destination = "ThirdSuppliersPooling")
    public void thirdSuppliersPooling(String messageBody) {
        System.out.println("Received message: " + messageBody);
    }


    public void sendMessageToPoolingThirdSuppliers(List<CompanyThirdSuppliersToPoolingDTO> itemsToPooling) throws JsonProcessingException {
        ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName("ThirdSuppliersPooling")
                .buildClient();

        ServiceBusMessageBatch batch = senderClient.createMessageBatch();

        for (CompanyThirdSuppliersToPoolingDTO dto : itemsToPooling) {
            String json = objectMapper.writeValueAsString(dto);
//            System.out.println("Size of DTO in bytes: " + json.getBytes(StandardCharsets.UTF_8).length);

            ServiceBusMessage message = new ServiceBusMessage(json);
            if (!batch.tryAddMessage(message)) {
                senderClient.sendMessages(batch);
                batch = senderClient.createMessageBatch();
                batch.tryAddMessage(message);
            }
        }

        if (batch.getCount() > 0) {
            senderClient.sendMessages(batch);
        }
    }
}
