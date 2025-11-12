package com.RestaurantSystem.WebSocket;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Shift.DTOs.ShiftOperationDTO;
import com.RestaurantSystem.Services.ShiftService;
import com.RestaurantSystem.WebSocket.DTOs.SignalRConnectionDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SignalR {
    @Value("${func-negotiate-sigr}")
    private String funcNegotiateSigr;

    @Value("${signalR-url-connection}")
    private String signalRUrlConnection;

    @Value("${signalR-access-key}")
    private String signalRAccessKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String hubName = "serverless";

    private final ShiftService shiftService;

    public SignalR(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    // <>------------- Methods ------------- <>
    public ResponseEntity<SignalRConnectionDTO> negotiate() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<SignalRConnectionDTO> response =
                restTemplate.exchange(funcNegotiateSigr, HttpMethod.POST, entity, SignalRConnectionDTO.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    public void addToGroup(String connectionID, String group) {
        try {
            String url = signalRUrlConnection + "/api/v1/hubs/" + hubName + "/connections/" + connectionID + "/groups/" + group;

            String token = JWT.create()
                    .withAudience(url)
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                    .sign(Algorithm.HMAC256(signalRAccessKey));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(null, headers);
            restTemplate.put(url, entity, String.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToAll(String message) {
        String url = signalRUrlConnection + "/api/v1/hubs/" + hubName;

        sendMessage(url, message);
    }

    public void sendMessageToGroup(String message, String group) {
        String url = signalRUrlConnection + "/api/v1/hubs/" + hubName + "/groups/" + group;

        sendMessage(url, message);
    }

    public void sendShiftOperationSigr(Company company) {
        String url = signalRUrlConnection + "/api/v1/hubs/" + hubName + "/groups/" + company.getId();
        ShiftOperationDTO shiftOperation = shiftService.getShiftOperationRequesterAlreadyVerified(company);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try{
            String messageJson = mapper.writeValueAsString(shiftOperation);
            sendMessage(url, messageJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // <>------------- Private Methods ------------- <>
    private void sendMessage(String url, String message) {
        try {
            String token = JWT.create()
                    .withAudience(url)
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                    .sign(Algorithm.HMAC256(signalRAccessKey));

            Map<String, Object> body = new HashMap<>();
            body.put("target", "ReceiveMessage");
            body.put("arguments", new String[]{message});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
