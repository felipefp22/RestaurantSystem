//package com.RestaurantSystem.Services.WebRequests;
//
//
//import com.RestaurantSystem.Entities.Company.CompanyIFood;
//import com.RestaurantSystem.Repositories.CompanyIFoodRepo;
//import com.RestaurantSystem.Services.WebRequests.IFoodDTOs.IFoodTokenReturnDTO;
//import io.netty.resolver.DefaultAddressResolverGroup;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientException;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import reactor.netty.http.client.HttpClient;
//
//import java.util.Map;
//
//@Component
//public class WebClientLinkRequestNineNine {
//    @Value("${ninenine.client-id}")
//    private String nineNineClientID;
//    @Value("${ninenine.client-secret}")
//    private String nineNineClientSecret;
//
//    private final String nineNineURL;
//    private final CompanyIFoodRepo companyIFoodRepo;
//    private WebClient webClient;
//
//    public WebClientLinkRequestNineNine(CompanyIFoodRepo companyNineNineRepo, @Value("${ninenine.url}") String nineNineURL) {
//        this.companyIFoodRepo = companyNineNineRepo;
//
//        this.nineNineURL = nineNineURL;
//        webClient = WebClient.builder()
//                .baseUrl(nineNineURL)
//                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
//                        .resolver(DefaultAddressResolverGroup.INSTANCE)))
//                .build();
//    }
//
//    // <>---|Methods|-----------------------------------------------<>
//
//    public <T> T requisitionGenericNineNine(CompanyIFood companyNineNine, String uri, HttpMethod httpMethod, Object requestBody,
//                                            ParameterizedTypeReference<T> responseType, Map<String, String> headers) {
//
//        return retryRequestNineNine(companyNineNine, uri, httpMethod, requestBody, responseType,
//                headers, 5);
//    }
//
//    public <T> T retryRequestNineNine(CompanyIFood companyNineNine, String uri, HttpMethod httpMethod, Object requestBody,
//                                      ParameterizedTypeReference<T> responseType, Map<String, String> headers, int remainingRetries) {
//
//        try {
//            return webClient
//                    .method(httpMethod)
//                    .uri(uri)
//                    .headers(httpHeaders -> {
//                        if (headers != null) {
//                            headers.forEach(httpHeaders::add);
//                        }
//                        httpHeaders.add("Authorization", companyNineNine.getAccessToken());
//                    })
//                    .body(requestBody != null ? BodyInserters.fromValue(requestBody) : null)
//                    .retrieve()
//                    .bodyToMono(responseType)
//                    .block();
//
//        } catch (WebClientException e) {
//            if (e instanceof WebClientResponseException && remainingRetries > 0) {
//                WebClientResponseException ex = (WebClientResponseException) e;
//                if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED) || ex.getStatusCode().equals(HttpStatus.FORBIDDEN)
//                        || ex.getStatusCode().equals(HttpStatus.valueOf(503))) {
//                    // Call your login method here
//                    getCompanyAccessToken(companyNineNine);
//                    // Retry the request
//                    return retryRequestNineNine(companyNineNine, uri, httpMethod, requestBody, responseType, headers, remainingRetries - 1);
//                }
//            }
//            throw e;
//        }
//    }
//
//    public void getCompanyAccessToken(CompanyIFood companyNineNine) {
//        var requisitionPath = ("/authentication/v1.0/oauth/token");
//
//        IFoodTokenReturnDTO responseFrommNineNine = webClient
//                .method(HttpMethod.POST)
//                .uri(requisitionPath)
//                .body(BodyInserters.fromFormData("grantType", "refresh_token")
//                        .with("clientId", nineNineClientID)
//                        .with("clientSecret", nineNineClientSecret)
//                        .with("refreshToken", companyNineNine.getRefreshToken()))
//                .retrieve()
//                .bodyToMono(IFoodTokenReturnDTO.class)
//                .block();
//
//        companyNineNine.setAccessToken("Bearer " + responseFrommNineNine.accessToken());
//        companyNineNine.setRefreshToken(responseFrommNineNine.refreshToken());
//        companyIFoodRepo.save(companyNineNine);
//    }
//
//    public String getNineNineClientID() {
//        return nineNineClientID;
//    }
//
//    public String getNineNineClientSecret() {
//        return nineNineClientSecret;
//    }
//
//    public String getNineNineURL() {
//        return nineNineURL;
//    }
//}