package com.RestaurantSystem.Services.WebRequests;


import com.RestaurantSystem.Entities.Company.CompanyIFood;
import com.RestaurantSystem.Repositories.CompanyIFoodRepo;
import com.RestaurantSystem.Services.WebRequests.DTOs.IFoodRequestTokenDTO;
import com.RestaurantSystem.Services.WebRequests.DTOs.IFoodTokenReturnDTO;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

@Component
public class WebClientLinkRequestIFood {
    @Value("${ifood.client-id}")
    private String ifoodClientID;
    @Value("${ifood.client-secret}")
    private String ifoodClientSecret;

    private final String ifoodURL;
    private final CompanyIFoodRepo companyIFoodRepo;
    private WebClient webClient;

    public WebClientLinkRequestIFood(CompanyIFoodRepo companyIFoodRepo, @Value("${ifood.url}") String ifoodUrl) {
        this.companyIFoodRepo = companyIFoodRepo;

        this.ifoodURL = ifoodUrl;
        webClient = WebClient.builder()
                .baseUrl(ifoodUrl)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .resolver(DefaultAddressResolverGroup.INSTANCE)))
                .build();
    }

    // <>---|Methods|-----------------------------------------------<>

    public <T> T requisitionGenericIFood(CompanyIFood companyIfood, String uri, HttpMethod httpMethod, Object requestBody,
                                                ParameterizedTypeReference<T> responseType, Map<String, String> headers) {

        return retryRequestIFood(companyIfood, uri, httpMethod, requestBody, responseType,
                headers, 5);
    }

    public <T> T retryRequestIFood(CompanyIFood companyIfood, String uri, HttpMethod httpMethod, Object requestBody,
                                          ParameterizedTypeReference<T> responseType, Map<String, String> headers, int remainingRetries) {

        try {
            return webClient
                    .method(httpMethod)
                    .uri(uri)
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            headers.forEach(httpHeaders::add);
                        }
                        httpHeaders.add("Authorization", companyIfood.getAccessToken());
                    })
                    .body(requestBody != null ? BodyInserters.fromValue(requestBody) : null)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();

        } catch (WebClientException e) {
            if (e instanceof WebClientResponseException && remainingRetries > 0) {
                WebClientResponseException ex = (WebClientResponseException) e;
                if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED) || ex.getStatusCode().equals(HttpStatus.FORBIDDEN)
                        || ex.getStatusCode().equals(HttpStatus.valueOf(503))) {
                    // Call your login method here
                    getCompanyAccessToken(companyIfood);
                    // Retry the request
                    return retryRequestIFood(companyIfood, uri, httpMethod, requestBody, responseType, headers, remainingRetries - 1);
                }
            }
            throw e;
        }
    }

    public void getCompanyAccessToken(CompanyIFood companyIfood) {
        var requisitionPath = ("/authentication/v1.0/oauth/token");

        IFoodTokenReturnDTO responseFrommIFood = webClient
                .method(HttpMethod.POST)
                .uri(requisitionPath)
                .body(BodyInserters.fromFormData("grantType", "refresh_token")
                        .with("clientId", ifoodClientID)
                        .with("clientSecret", ifoodClientSecret)
                        .with("refreshToken", companyIfood.getRefreshToken()))
                .retrieve()
                .bodyToMono(IFoodTokenReturnDTO.class)
                .block();

        companyIfood.setAccessToken("Bearer " + responseFrommIFood.accessToken());
        companyIfood.setRefreshToken(responseFrommIFood.refreshToken());
        companyIFoodRepo.save(companyIfood);
    }

    public String getIfoodClientID() {
        return ifoodClientID;
    }

    public String getIfoodClientSecret() {
        return ifoodClientSecret;
    }

    public String getIfoodURL() {
        return ifoodURL;
    }
}