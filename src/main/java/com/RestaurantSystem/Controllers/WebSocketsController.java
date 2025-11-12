package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.WebSocket.DTOs.SignalRConnectionDTO;
import com.RestaurantSystem.WebSocket.MyWebSocketHandler;
import com.RestaurantSystem.WebSocket.SignalR;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/webs")
public class WebSocketsController {
    private final MyWebSocketHandler handler;
    private final SignalR signalR;
    private final RestTemplate restTemplate = new RestTemplate();

    public WebSocketsController(MyWebSocketHandler handler, SignalR signalR) {
        this.handler = handler;
        this.signalR = signalR;
    }

    // <> ------------- Methods ------------- <>
    @PostMapping("/negotiate")
    public ResponseEntity<SignalRConnectionDTO> negotiate() throws Exception {
        ResponseEntity<SignalRConnectionDTO> response = signalR.negotiate();

        return response;
    }

    @PutMapping("/addToGroup/{connectionID}/{group}")
    public void addToGroup(@PathVariable String connectionID, @PathVariable String group) {

        signalR.addToGroup(connectionID, group);
    }

    @PostMapping("/send-all")
    public void sendMessageToAll(@RequestBody String message) {

        signalR.sendMessageToAll(message);
    }

    @PostMapping("/send-group/{group}")
    public void sendMessageToGroup(@RequestBody String message, @PathVariable String group) {

        signalR.sendMessageToGroup(message, group);
    }
}
