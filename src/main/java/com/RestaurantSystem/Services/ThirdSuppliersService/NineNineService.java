//package com.RestaurantSystem.Services.ThirdSuppliersService;
//
//import com.RestaurantSystem.Entities.Company.CompanyIFood;
//import com.RestaurantSystem.Entities.IFood.DTOs.AcknowledgeIFoodDTO;
//import com.RestaurantSystem.Entities.IFood.DTOs.OrderDetailsIFoodDTO;
//import com.RestaurantSystem.Entities.NineNine.NineNineAuthOrderIDTO;
//import com.RestaurantSystem.Entities.NineNine.OrderDetailsNineNineDTO;
//import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
//import com.RestaurantSystem.Services.WebRequests.IFoodDTOs.*;
//import com.RestaurantSystem.Services.WebRequests.WebClientLinkRequestNineNine;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpMethod;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.List;
//
//@Service
//public class NineNineService {
//
//    private WebClient webClient;
//    private final VerificationsServices verificationsServices;
//    private final WebClientLinkRequestNineNine webClientLinkRequestNineNine;
//
//    public NineNineService(VerificationsServices verificationsServices, WebClientLinkRequestNineNine webClientLinkRequestNineNine) {
//        this.verificationsServices = verificationsServices;
//        this.webClientLinkRequestNineNine = webClientLinkRequestNineNine;
//    }
//
//
//    // <> ------------- Methods ------------- <>
//    public List<IFoodMerchantDataDTO> getConnectedNineNineStore(String requesterID, UUID companyID) {
//        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
//        Company company = verificationsServices.retrieveCompany(companyID);
//        verificationsServices.justOwnerOrManager(company, requester);
//        CompanyIFood companyIFoodData = company.getCompanyIFoodData();
//
//        var responseFromIFood = webClientLinkRequestIFood.requisitionGenericNineNine(companyIFoodData, "/merchant/v1.0/merchants", HttpMethod.GET, null,
//                new ParameterizedTypeReference<List<IFoodMerchantDataDTO>>() {
//                }, null);
//
//        return responseFromIFood;
//    }
//
//    public ReturnIFoodCodeToUserDTO createUserCode(String requesterID, UUID companyID) {
//        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
//        Company company = verificationsServices.retrieveCompany(companyID);
//        verificationsServices.justOwnerOrManager(company, requester);
//
//        CompanyIFood companyNineNineData = company.getCompanyIFoodData();
//        if (companyNineNineData == null) companyNineNineData = new CompanyIFood();
//        if (companyNineNineData.getRefreshToken() != null) {
//            throw new RuntimeException("iFood user code already created for this company");
//        }
//
//        IFoodUserCodeDTO responseFrommIFood = webClient
//                .method(HttpMethod.POST)
//                .uri("/authentication/v1.0/oauth/userCode")
//                .body(BodyInserters.fromFormData("grantType", "refresh_token")
//                        .with("clientId", webClientLinkRequestIFood.getNineNineClientID()))
//                .retrieve()
//                .bodyToMono(IFoodUserCodeDTO.class)
//                .block();
//
//        companyNineNineData.setLastGeneratedUserCode(responseFrommIFood.userCode());
//        companyNineNineData.setLastGeneratedAuthorizationCodeVerifier(responseFrommIFood.authorizationCodeVerifier());
//        companyNineNineData.setLastGeneratedFriendlyUrlUserCode(responseFrommIFood.verificationUrlComplete());
//        company.setCompanyIFoodData(companyNineNineData);
//        companyIFoodRepo.save(companyNineNineData);
//        companyRepo.save(company);
//
//        return new ReturnIFoodCodeToUserDTO(companyNineNineData.getLastGeneratedUserCode(), companyNineNineData.getLastGeneratedFriendlyUrlUserCode());
//    }
//
//    public void registerAuthorizeUserCode(String requesterID, ReceiveCustomerCodeToRegisterIFoodDTO dto) {
//        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
//        Company company = verificationsServices.retrieveCompany(dto.companyID());
//        verificationsServices.justOwnerOrManager(company, requester);
//        CompanyIFood companyNineNineData = company.getCompanyIFoodData();
//
//        IFoodTokenReturnDTO responseFrommIFood = webClient
//                .method(HttpMethod.POST)
//                .uri("/authentication/v1.0/oauth/token")
//                .body(BodyInserters.fromFormData("grantType", "authorization_code")
//                        .with("clientId", webClientLinkRequestIFood.getNineNineClientID())
//                        .with("clientSecret", webClientLinkRequestIFood.getNineNineClientSecret())
//                        .with("authorizationCode", dto.code())
//                        .with("authorizationCodeVerifier", companyNineNineData.getLastGeneratedAuthorizationCodeVerifier()))
//                .retrieve()
//                .bodyToMono(IFoodTokenReturnDTO.class)
//                .block();
//
//        companyNineNineData.setStoreCode(dto.code());
//        companyNineNineData.setStoreAuthorizationCodeVerifier(companyNineNineData.getLastGeneratedAuthorizationCodeVerifier());
//        companyNineNineData.setAccessToken("Bearer " + responseFrommIFood.accessToken());
//        companyNineNineData.setRefreshToken(responseFrommIFood.refreshToken());
//
//        List<IFoodMerchantDataDTO> merchantData = getConnectedIFoodStore(requesterID, dto.companyID());
////        companyNineNineData.setMerchantID(merchantData.id());
////        companyNineNineData.setMerchantName(merchantData.name());
////        companyNineNineData.setCorporateName(merchantData.corporateName());
//        companyIFoodRepo.save(companyNineNineData);
//    }
//
//    public void disconnectNineNineStore(String requesterID, UUID companyID) {
//        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
//        Company company = verificationsServices.retrieveCompany(companyID);
//        verificationsServices.justOwnerOrManager(company, requester);
//        CompanyIFood companyNineNineData = company.getCompanyIFoodData();
//
//        company.setCompanyIFoodData(null);
//        companyIFoodRepo.delete(companyNineNineData);
//        companyRepo.save(company);
//    }
//
//    // <>------------- Pooling -------------<>
//    public void poolingNineNineHandle(CompanyThirdSuppliersToPoolingDTO dto) {
//        if (dto.companyIFoodData() == null) return;
//
//        Optional<List<EventsIFoodDTO>> nineNineEvents = webClientLinkRequestIFood.requisitionGenericNineNine(dto.companyIFoodData(),
//                "events/v1.0/events:polling", HttpMethod.GET, null, new ParameterizedTypeReference<Optional<List<EventsIFoodDTO>>>() {
//                }, null);
//
//        if (nineNineEvents == null) return;
//        nineNineEvents.get().forEach(x -> {
//            var orderDetails = getIFoodOrderDetails(dto.companyIFoodData(), x.orderId());
//
//            System.out.println("Order Details 99: " + orderDetails.toString());
//        });
//        acknowledgeEventIFood(dto.companyIFoodData(), nineNineEvents.get().stream().map(x -> new AcknowledgeIFoodDTO(x.id())).toList());
//
//    }
//
//    // <>------------- IFood Events Actions -------------<>
//    private void acknowledgeEventNineNine(CompanyIFood companyIFood, List<AcknowledgeIFoodDTO> acknowledgeDTO) {
//        webClientLinkRequestNineNine.requisitionGenericNineNine(companyIFood,
//                "/v1/order/order/acknowledgment", HttpMethod.POST, acknowledgeDTO,
//                new ParameterizedTypeReference<Void>() {
//                }, null);
//    }
//
//    private OrderDetailsNineNineDTO getNineNineOrderDetails(CompanyIFood companyNineNine, String orderID) {
//        return webClientLinkRequestNineNine.requisitionGenericNineNine(companyNineNine,
//                "/v1/order/order/detail?auth_token=" + authToken + "&order_id=" + orderID, HttpMethod.GET, null,
//                new ParameterizedTypeReference<OrderDetailsNineNineDTO>() {
//                }, null);
//    }
//
//    private void confirmOrderNineNine(CompanyIFood companyNineNine, String orderID) {
//        webClientLinkRequestNineNine.requisitionGenericNineNine(companyNineNine,
//                "/v1/order/order/confirm" + orderID, HttpMethod.POST, new NineNineAuthOrderIDTO(authToken, orderID),
//                new ParameterizedTypeReference<Void>() {
//                }, null);
//    }
//
//    private void dispatchNineNine(CompanyIFood companyNineNine, String orderID) {
//        webClientLinkRequestNineNine.requisitionGenericNineNine(companyNineNine,
//                "/order/v1.0/orders/" + orderID + "/dispatch", HttpMethod.POST, null,
//                new ParameterizedTypeReference<Void>() {
//                }, null);
//    }
//
//    private void readyToPickupNineNine(CompanyIFood companyNineNine, String orderID) {
//        webClientLinkRequestNineNine.requisitionGenericNineNine(companyNineNine,
//                "/v1/order/order/ready?auth_token=" + authToken + "&order_id=" + orderID, HttpMethod.GET, null,
//                new ParameterizedTypeReference<Void>() {
//                }, null);
//    }
//
//    private void deliveredNineNine(CompanyIFood companyNineNine, String orderID) {
//        webClientLinkRequestNineNine.requisitionGenericNineNine(companyNineNine,
//                "/v1/order/order/delivered?auth_token="+ authToken + "&order_id=" + orderID, HttpMethod.GET, null,
//                new ParameterizedTypeReference<Void>() {
//                }, null);
//    }
//}
