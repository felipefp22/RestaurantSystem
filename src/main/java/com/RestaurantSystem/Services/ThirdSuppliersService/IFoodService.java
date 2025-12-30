package com.RestaurantSystem.Services.ThirdSuppliersService;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyIfood;
import com.RestaurantSystem.Entities.Company.DTOs.CompanyThirdSuppliersToPoolingDTO;
import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.OrderItemDTO;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Product.ProductOption;
import com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs.*;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.CompanyIFoodRepo;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import com.RestaurantSystem.Services.OrderService;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class IFoodService {

    private WebClient webClient;
    private final CompanyIFoodRepo companyIFoodRepo;
    private final CompanyRepo companyRepo;
    private final VerificationsServices verificationsServices;
    private final WebClientLinkRequestIFood webClientLinkRequestIFood;
    private final OrderService orderService;

    public IFoodService(VerificationsServices verificationsServices, @Value("${ifood.url}") String ifoodURL, CompanyIFoodRepo companyIFoodRepo, CompanyRepo companyRepo, WebClientLinkRequestIFood webClientLinkRequestIFood, OrderService orderService) {
        webClient = WebClient.builder()
                .baseUrl(ifoodURL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .resolver(DefaultAddressResolverGroup.INSTANCE)))
                .build();
        this.verificationsServices = verificationsServices;
        this.companyIFoodRepo = companyIFoodRepo;
        this.companyRepo = companyRepo;
        this.webClientLinkRequestIFood = webClientLinkRequestIFood;
        this.orderService = orderService;
    }


    // <> ------------- Methods ------------- <>
    public Boolean hasIFoodConnection(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);
        CompanyIfood companyIfoodData = company.getCompanyIFoodData();

        return companyIfoodData != null && companyIfoodData.getRefreshToken() != null;
    }

    public List<MerchantDataIFoodDTO> getConnectedIFoodStore(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);
        CompanyIfood companyIfoodData = company.getCompanyIFoodData();

        if (companyIfoodData == null)
            throw new RuntimeException("No iFood store connected to this company");

        var responseFromIFood = webClientLinkRequestIFood.requisitionGenericIFood(companyIfoodData, "/merchant/v1.0/merchants", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<MerchantDataIFoodDTO>>() {
                }, null);

        return responseFromIFood;
    }

    public ReturnIFoodCodeToUserDTO createUserCode(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);

        CompanyIfood companyIfoodData = company.getCompanyIFoodData();
        if (companyIfoodData == null) companyIfoodData = new CompanyIfood(company);
        if (companyIfoodData.getRefreshToken() != null) {
            throw new RuntimeException("iFood user code already created for this company");
        }

        UserCodeIFoodDTO responseFrommIFood = webClient
                .method(HttpMethod.POST)
                .uri("/authentication/v1.0/oauth/userCode")
                .body(BodyInserters.fromFormData("grantType", "refresh_token")
                        .with("clientId", webClientLinkRequestIFood.getIfoodClientID()))
                .retrieve()
                .bodyToMono(UserCodeIFoodDTO.class)
                .block();

        companyIfoodData.setLastGeneratedUserCode(responseFrommIFood.userCode());
        companyIfoodData.setLastGeneratedAuthorizationCodeVerifier(responseFrommIFood.authorizationCodeVerifier());
        companyIfoodData.setLastGeneratedFriendlyUrlUserCode(responseFrommIFood.verificationUrlComplete());
        company.setCompanyIFoodData(companyIfoodData);
        companyIFoodRepo.save(companyIfoodData);
        companyRepo.save(company);

        return new ReturnIFoodCodeToUserDTO(companyIfoodData.getLastGeneratedUserCode(), companyIfoodData.getLastGeneratedFriendlyUrlUserCode());
    }

    public void registerAuthorizeUserCode(String requesterID, ReceiveCustomerCodeToRegisterIFoodDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManager(company, requester);
        CompanyIfood companyIfoodData = company.getCompanyIFoodData();

        TokenReturnIFoodDTO responseFrommIFood = webClient
                .method(HttpMethod.POST)
                .uri("/authentication/v1.0/oauth/token")
                .body(BodyInserters.fromFormData("grantType", "authorization_code")
                        .with("clientId", webClientLinkRequestIFood.getIfoodClientID())
                        .with("clientSecret", webClientLinkRequestIFood.getIfoodClientSecret())
                        .with("authorizationCode", dto.code())
                        .with("authorizationCodeVerifier", companyIfoodData.getLastGeneratedAuthorizationCodeVerifier()))
                .retrieve()
                .bodyToMono(TokenReturnIFoodDTO.class)
                .block();

        companyIfoodData.setStoreCode(dto.code());
        companyIfoodData.setStoreAuthorizationCodeVerifier(companyIfoodData.getLastGeneratedAuthorizationCodeVerifier());
        companyIfoodData.setAccessToken("Bearer " + responseFrommIFood.accessToken());
        companyIfoodData.setRefreshToken(responseFrommIFood.refreshToken());

        List<MerchantDataIFoodDTO> merchantData = getConnectedIFoodStore(requesterID, dto.companyID());
//        companyIFoodData.setMerchantID(merchantData.id());
//        companyIFoodData.setMerchantName(merchantData.name());
//        companyIFoodData.setCorporateName(merchantData.corporateName());
        companyIFoodRepo.save(companyIfoodData);
    }

    public void disconnectIFoodStore(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.justOwnerOrManager(company, requester);
        CompanyIfood companyIfoodData = company.getCompanyIFoodData();

        company.setCompanyIFoodData(null);
        companyIFoodRepo.delete(companyIfoodData);
        companyRepo.save(company);
    }

    // <>------------- Pooling -------------<>
    public void poolingIFoodHandle(CompanyThirdSuppliersToPoolingDTO dto) {
        if (dto.companyIfoodData() == null) return;

        Optional<List<EventsIFoodDTO>> ifoodEvents = webClientLinkRequestIFood.requisitionGenericIFood(dto.companyIfoodData(),
                "events/v1.0/events:polling", HttpMethod.GET, null, new ParameterizedTypeReference<Optional<List<EventsIFoodDTO>>>() {
                }, null);
        if (ifoodEvents == null) return;

        ifoodEvents.get().forEach(x -> {
            Company company = verificationsServices.retrieveCompany(dto.companyId());
            OrderDetailsIFoodDTO ifoodOrderDetails = getIFoodOrderDetails(dto.companyIfoodData(), x.orderId());

//            String tableNumberOrDeliveryOrPickup = "delivery"; //Still needs do make logica
//            CreateThirdSpOrderDTO thirdSpDTO = new CreateThirdSpOrderDTO(dto, ifoodOrderDetails, tableNumberOrDeliveryOrPickup);
//            orderService.createThirdSupplierOrder(thirdSpDTO);
            System.out.println("sa");
        });

        acknowledgeEventIFood(dto.companyIfoodData(), ifoodEvents.get().stream().map(x -> new AcknowledgeIFoodDTO(x.id())).toList());

    }

    private List<OrderItemDTO> createOrderItemDTO(Company company, OrderDetailsIFoodDTO ifoodOrderDetails) {
        Map<String, Product> productMap = company.getProductsCategories().stream()
                .flatMap(c -> c.getProducts().stream())
                .collect(Collectors.toMap(Product::getIfoodCode, p -> p));

        Map<String, ProductOption> productOptsMap = company.getProductsCategories().stream()
                .flatMap(c -> c.getProductOptions().stream())
                .collect(Collectors.toMap(ProductOption::getIfoodCode, p -> p));

        List<OrderItemDTO> orderItemsDTO = new ArrayList<>();

        ifoodOrderDetails.items().forEach(x -> {
            List<String> productsPdvCodes = IntStream.range(0, x.quantity()).mapToObj(i -> x.externalCode()).toList();
            List<String> productOptsPdvCodes =
                    x.options().stream().flatMap(opt -> IntStream.range(0, opt.quantity()).mapToObj(z -> opt.externalCode())).toList();

            List<String> productsIDs = productsPdvCodes.stream().map(pdvCode -> productMap.get(pdvCode).getId().toString()).toList();
            List<String> productOptsIDs = productOptsPdvCodes.stream().map(pdvCode -> productOptsMap.get(pdvCode).getId().toString()).toList();
            orderItemsDTO.add(new OrderItemDTO(productsIDs, productOptsIDs, x.observations(), 0.0));
        });

        return orderItemsDTO;
    }

    // <>------------- IFood Events Actions -------------<>
    private void acknowledgeEventIFood(CompanyIfood companyIFood, List<AcknowledgeIFoodDTO> acknowledgeDTO) {
        webClientLinkRequestIFood.requisitionGenericIFood(companyIFood,
                "/events/v1.0/events/acknowledgment", HttpMethod.POST, acknowledgeDTO,
                new ParameterizedTypeReference<Void>() {
                }, null);
    }

    private OrderDetailsIFoodDTO getIFoodOrderDetails(CompanyIfood companyIFood, String orderID) {
        return webClientLinkRequestIFood.requisitionGenericIFood(companyIFood,
                "/order/v1.0/orders/" + orderID, HttpMethod.GET, null,
                new ParameterizedTypeReference<OrderDetailsIFoodDTO>() {
                }, null);
    }

    private void confirmOrderIFood(CompanyIfood companyIFood, String orderID) {
        webClientLinkRequestIFood.requisitionGenericIFood(companyIFood,
                "/order/v1.0/orders/" + orderID + "/confirm", HttpMethod.POST, null,
                new ParameterizedTypeReference<Void>() {
                }, null);
    }

    private void dispatchIFood(CompanyIfood companyIFood, String orderID) {
        webClientLinkRequestIFood.requisitionGenericIFood(companyIFood,
                "/order/v1.0/orders/" + orderID + "/dispatch", HttpMethod.POST, null,
                new ParameterizedTypeReference<Void>() {
                }, null);
    }

    private void readyToPickupIFood(CompanyIfood companyIFood, String orderID) {
        webClientLinkRequestIFood.requisitionGenericIFood(companyIFood,
                "/order/v1.0/orders/" + orderID + "/readyToPickup", HttpMethod.POST, null,
                new ParameterizedTypeReference<Void>() {
                }, null);
    }

    private void startPreparationIFood(CompanyIfood companyIFood, String orderID) {
        webClientLinkRequestIFood.requisitionGenericIFood(companyIFood,
                "/order/v1.0/orders/" + orderID + "/startPreparation", HttpMethod.POST, null,
                new ParameterizedTypeReference<Void>() {
                }, null);
    }

    private void deliveredIFood(CompanyIfood companyIFood, String orderID) {
        webClientLinkRequestIFood.requisitionGenericIFood(companyIFood,
                "/order/v1.0/orders/" + orderID + "/arrivedAtDestination", HttpMethod.POST, null,
                new ParameterizedTypeReference<Void>() {
                }, null);
    }
}
