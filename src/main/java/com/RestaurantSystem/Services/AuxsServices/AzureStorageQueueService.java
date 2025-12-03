//package com.RestaurantSystem.Services.AuxsServices;
//
//import com.azure.storage.queue.QueueClient;
//import com.azure.storage.queue.QueueClientBuilder;
//import com.azure.storage.queue.models.QueueMessageItem;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AzureStorageQueueService {
//
//    @Value("${azure.storage.connection-string}")
//    private String connectionString;
//
//    private QueueClient getClient(String queueName) {
//        return new QueueClientBuilder()
//                .connectionString(connectionString)
//                .queueName(queueName)
//                .buildClient();
//    }
//
//    // <>--------------- Methods ---------------<>
//    public void sendMessage(String queueName, String message) {
//        QueueClient client = getClient(queueName);
//        client.createIfNotExists();
//        client.sendMessage(message);
//    }
//
//    public QueueMessageItem receiveMessage(String queueName) {
//        QueueClient client = getClient(queueName);
//        return client.receiveMessage();  // returns null if empty
//    }
//
//    public void deleteMessage(String queueName, QueueMessageItem message) {
//        QueueClient client = getClient(queueName);
//        client.deleteMessage(message.getMessageId(), message.getPopReceipt());
//    }
//}
