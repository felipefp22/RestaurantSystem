package com.RestaurantSystem.Services.ThirdSuppliersService;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyIFood;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.CompanyIFoodRepo;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import com.RestaurantSystem.Services.WebRequests.DTOs.*;
import com.RestaurantSystem.Services.WebRequests.WebClientLinkRequestIFood;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.UUID;

@Service
public class IFoodService {

    private WebClient webClient;
    private final CompanyIFoodRepo companyIFoodRepo;
    private final CompanyRepo companyRepo;
    private final VerificationsServices verificationsServices;
    private final WebClientLinkRequestIFood webClientLinkRequestIFood;

    public IFoodService(VerificationsServices verificationsServices, @Value("${ifood.url}") String ifoodURL, CompanyIFoodRepo companyIFoodRepo, CompanyRepo companyRepo, WebClientLinkRequestIFood webClientLinkRequestIFood) {
        webClient = WebClient.builder()
                .baseUrl(ifoodURL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .resolver(DefaultAddressResolverGroup.INSTANCE)))
                .build();
        this.verificationsServices = verificationsServices;
        this.companyIFoodRepo = companyIFoodRepo;
        this.companyRepo = companyRepo;
        this.webClientLinkRequestIFood = webClientLinkRequestIFood;
    }


    // <> ------------- Methods ------------- <>
    public List<IFoodMerchantDataDTO> getConnectedIFoodStore(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);
        CompanyIFood companyIFoodData = company.getCompanyIFoodData();

        var responseFromIFood = webClientLinkRequestIFood.requisitionGenericIFood(companyIFoodData, "/merchant/v1.0/merchants", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<IFoodMerchantDataDTO>>() {
                }, null);

        return responseFromIFood;
    }

    public ReturnIFoodCodeToUserDTO createUserCode(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);

        CompanyIFood companyIFoodData = company.getCompanyIFoodData();
        if (companyIFoodData == null) companyIFoodData = new CompanyIFood();
        if (companyIFoodData.getRefreshToken() != null) {
            throw new RuntimeException("iFood user code already created for this company");
        }

        IFoodUserCodeDTO responseFrommIFood = webClient
                .method(HttpMethod.POST)
                .uri("/authentication/v1.0/oauth/userCode")
                .body(BodyInserters.fromFormData("grantType", "refresh_token")
                        .with("clientId", webClientLinkRequestIFood.getIfoodClientID()))
                .retrieve()
                .bodyToMono(IFoodUserCodeDTO.class)
                .block();

        companyIFoodData.setLastGeneratedUserCode(responseFrommIFood.userCode());
        companyIFoodData.setLastGeneratedAuthorizationCodeVerifier(responseFrommIFood.authorizationCodeVerifier());
        companyIFoodData.setLastGeneratedFriendlyUrlUserCode(responseFrommIFood.verificationUrlComplete());
        company.setCompanyIFoodData(companyIFoodData);
        companyIFoodRepo.save(companyIFoodData);
        companyRepo.save(company);

        return new ReturnIFoodCodeToUserDTO(companyIFoodData.getLastGeneratedUserCode(), companyIFoodData.getLastGeneratedFriendlyUrlUserCode());
    }

    public void registerAuthorizeUserCode(String requesterID, ReceiveCustomerCodeToRegisterIFoodDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManager(company, requester);
        CompanyIFood companyIFoodData = company.getCompanyIFoodData();

        IFoodTokenReturnDTO responseFrommIFood = webClient
                .method(HttpMethod.POST)
                .uri("/authentication/v1.0/oauth/token")
                .body(BodyInserters.fromFormData("grantType", "authorization_code")
                        .with("clientId", webClientLinkRequestIFood.getIfoodClientID())
                        .with("clientSecret", webClientLinkRequestIFood.getIfoodClientSecret())
                        .with("authorizationCode", dto.code())
                        .with("authorizationCodeVerifier", companyIFoodData.getLastGeneratedAuthorizationCodeVerifier()))
                .retrieve()
                .bodyToMono(IFoodTokenReturnDTO.class)
                .block();

        companyIFoodData.setStoreCode(dto.code());
        companyIFoodData.setStoreAuthorizationCodeVerifier(companyIFoodData.getLastGeneratedAuthorizationCodeVerifier());
        companyIFoodData.setAccessToken("Bearer " + responseFrommIFood.accessToken());
        companyIFoodData.setRefreshToken(responseFrommIFood.refreshToken());

        List<IFoodMerchantDataDTO> merchantData = getConnectedIFoodStore(requesterID, dto.companyID());
//        companyIFoodData.setMerchantID(merchantData.id());
//        companyIFoodData.setMerchantName(merchantData.name());
//        companyIFoodData.setCorporateName(merchantData.corporateName());
        companyIFoodRepo.save(companyIFoodData);
    }

    public void disconnectIFoodStore(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);
        CompanyIFood companyIFoodData = company.getCompanyIFoodData();

        company.setCompanyIFoodData(null);
        companyIFoodRepo.delete(companyIFoodData);
        companyRepo.save(company);
    }
}
