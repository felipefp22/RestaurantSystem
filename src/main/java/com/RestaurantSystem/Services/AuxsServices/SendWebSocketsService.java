//package com.RestaurantSystem.Services.AuxsServices;
//
//import com.RestaurantSystem.Entities.Company.Company;
//import com.RestaurantSystem.Services.ShiftService;
//import com.RestaurantSystem.WebSocket.MyWebSocketHandler;
//import org.springframework.stereotype.Service;
//
//@Service
//public class SendWebSocketsService {
//    private final MyWebSocketHandler handler;
//    private final ShiftService shiftService;
//
//    public SendWebSocketsService(MyWebSocketHandler handler, ShiftService shiftService) {
//        this.handler = handler;
//        this.shiftService = shiftService;
//    }
//
//    // <>------------ Methods ------------<>
//    public void sendShiftOperationWS(Company company) {
//        var message = shiftService.getShiftOperationRequesterAlreadyVerified(company);
//
//        try {
//            handler.sendMessageToAll(message.toString());
//        } catch (Exception e) {
//
//        }
//    }
//}
