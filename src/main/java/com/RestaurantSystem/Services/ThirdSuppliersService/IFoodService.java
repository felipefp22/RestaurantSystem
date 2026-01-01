package com.RestaurantSystem.Services.ThirdSuppliersService;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyIfood;
import com.RestaurantSystem.Entities.Company.DTOs.CompanyThirdSuppliersToPoolingDTO;
import com.RestaurantSystem.Entities.ENUMs.PrintCategory;
import com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs.OrderItemDTO;
import com.RestaurantSystem.Entities.Printer.PrintSync;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Product.ProductOption;
import com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs.*;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.CompanyIFoodRepo;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.PrintSyncRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import com.RestaurantSystem.Services.OrderService;
import com.RestaurantSystem.Services.WebRequests.WebClientLinkRequestIFood;
import io.netty.resolver.DefaultAddressResolverGroup;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class IFoodService {

    private final String noCodePvcAcceptedDonotThrowError = "semcodigo";
    private WebClient webClient;
    private final CompanyIFoodRepo companyIFoodRepo;
    private final CompanyRepo companyRepo;
    private final VerificationsServices verificationsServices;
    private final WebClientLinkRequestIFood webClientLinkRequestIFood;
    private final OrderService orderService;
    private final PrintSyncRepo printSyncRepo;

    public IFoodService(VerificationsServices verificationsServices, @Value("${ifood.url}") String ifoodURL, CompanyIFoodRepo companyIFoodRepo, CompanyRepo companyRepo, WebClientLinkRequestIFood webClientLinkRequestIFood, OrderService orderService, PrintSyncRepo printSyncRepo) {
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
        this.printSyncRepo = printSyncRepo;
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
    @Transactional
    public void poolingIFoodHandle(CompanyThirdSuppliersToPoolingDTO dto) {
        if (dto.companyIfoodData() == null) return;
        Company company = verificationsServices.retrieveCompany(dto.companyId());

        Optional<List<EventsIFoodDTO>> ifoodEvents = webClientLinkRequestIFood.requisitionGenericIFood(dto.companyIfoodData(),
                "events/v1.0/events:polling", HttpMethod.GET, null, new ParameterizedTypeReference<Optional<List<EventsIFoodDTO>>>() {
                }, null);
        if (ifoodEvents == null) return;

        ifoodEvents.get().stream().filter(ev -> Objects.equals(ev.fullCode(), "PLACED")).forEach(x -> {
            confirmOrderIFood(company.getCompanyIFoodData(), x.orderId());

            String ifoodOrderID = null;
            try {
                OrderDetailsIFoodDTO ifoodOrderDetails = getIFoodOrderDetails(dto.companyIfoodData(), x.orderId());
                ifoodOrderID = ifoodOrderDetails.displayId() != null ? ifoodOrderDetails.displayId() : ifoodOrderDetails.id();
                List<OrderItemDTO> orderItemsDTO = createOrderItemDTO(company, ifoodOrderDetails);
                iFoodCreateOrderDTO ifoodToCreateOrder = new iFoodCreateOrderDTO(company, ifoodOrderDetails, orderItemsDTO);
                System.out.println("ifoodToCreateOrder -> " + ifoodToCreateOrder);
            } catch (Exception e){
                System.out.println("Error on create iFood order: " + e.getMessage());
//                printSyncRepo.save(new PrintSync(company, PrintCategory.FULLORDER, "\n\n\nErro ao criar pedido iFood ID:\n " + ifoodOrderID + ". Verificar na plataforma iFood.\n\n[Contate nosso 'Comanda Rapida' Suporte e informe o erro]\n\n\n\n\n\n\n"));
            }
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

        // <>---------- LEGACY_PIZZA ----------<>
        ifoodOrderDetails.items().stream().filter(x -> x.type().equals("LEGACY_PIZZA")).forEach(x -> {
            IntStream.range(0, x.quantity()).forEach(nothing -> {
                AtomicBoolean pdvCodeError = new AtomicBoolean(false);
                List<OrderDetailsIFoodDTO.ItemsIfood.Options> toppings = x.options().stream().filter(pdi -> Objects.equals(pdi.type(), "TOPPING")).toList();
                List<OrderDetailsIFoodDTO.ItemsIfood.Options> crusts = x.options().stream().filter(pdi -> Objects.equals(pdi.type(), "CRUST")).toList();

                List<String> productsPdvCodes = toppings.stream().flatMap(z -> IntStream.range(0, z.quantity()).mapToObj(i -> z.externalCode()))
                        .filter(code -> {
                            if (code == null || code.isBlank()) {
                                pdvCodeError.set(true);
                                return false;
                            }
                            return true;
                        }).toList();

                List<String> productOptsPdvCodes = crusts.stream().flatMap(opt -> IntStream.range(0, opt.quantity()).mapToObj(z -> opt.externalCode()))
                        .filter(code -> {
                            if (code == null || code.isBlank()) {
                                pdvCodeError.set(true);
                                return false;
                            }
                            return true;
                        }).toList();

                List<String> productsIDs = getProductIDsFromPdvCodes(productMap, productsPdvCodes, pdvCodeError);
                List<String> productOptsIDs = getProductOptsIDsFromPdvCodes(productOptsMap, productOptsPdvCodes, pdvCodeError);

                String pdvCodeErrorMsg = !pdvCodeError.get() ? null : createErrorMsg(toppings, crusts);
                orderItemsDTO.add(new OrderItemDTO(productsIDs, productOptsIDs, x.observations(), pdvCodeErrorMsg, x.totalPrice()));
            });
        });

        // <>---------- DEFAULT ----------<>
        ifoodOrderDetails.items().stream().filter(x -> x.type().equals("DEFAULT")).forEach(x -> {
            IntStream.range(0, x.quantity()).forEach(nothing -> {
                AtomicBoolean pdvCodeError = new AtomicBoolean(false);
                Product product = productMap.get(x.externalCode());

                List<OrderDetailsIFoodDTO.ItemsIfood.Options> options = x.options() == null || x.options().isEmpty() ? List.of() : x.options().stream().toList();
                List<String> productOptsPdvCodes = options.stream().flatMap(opt -> IntStream.range(0, opt.quantity()).mapToObj(z -> opt.externalCode()))
                        .filter(code -> {
                            if (code == null || code.isBlank()) {
                                pdvCodeError.set(true);
                                return false;
                            }
                            return true;
                        }).toList();

                List<String> productsIDs = product != null ? List.of(product.getId().toString()) : List.of();
                List<String> productOptsIDs = getProductOptsIDsFromPdvCodes(productOptsMap, productOptsPdvCodes, pdvCodeError);
                if(product == null) pdvCodeError.set(true);

                String pdvCodeErrorMsg = (product != null ? "" : x.externalCode() + "|" + x.name() + " R$" + x.unitPrice() + " - produto nao encontrado") +
                        (!pdvCodeError.get() ? null : createErrorMsg(new ArrayList<>(), options));
                orderItemsDTO.add(new OrderItemDTO(productsIDs, productOptsIDs, x.observations(), pdvCodeErrorMsg, x.totalPrice()));
            });
        });

        return orderItemsDTO;
    }

    private List<String> getProductIDsFromPdvCodes(Map<String, Product> productMap, List<String> productsPdvCodes, AtomicBoolean pdvCodeError) {
        return productsPdvCodes.stream().map(pdvc -> {
                    Product product = productMap.get(pdvc);
                    if (product == null) {
                        if (!Objects.equals(pdvc, noCodePvcAcceptedDonotThrowError)) pdvCodeError.set(true);
                        return null;
                    }
                    return product.getId().toString();
                })
                .filter(Objects::nonNull).toList();
    }

    private List<String> getProductOptsIDsFromPdvCodes(Map<String, ProductOption> productOptsMap, List<String> productOptsPdvCodes, AtomicBoolean pdvCodeError) {
        return productOptsPdvCodes.stream().map(pdvc -> {
                    ProductOption productOpt = productOptsMap.get(pdvc);
                    if (productOpt == null) {
                        if (!Objects.equals(pdvc, noCodePvcAcceptedDonotThrowError)) pdvCodeError.set(true);
                        return null;
                    }
                    return productOpt.getId().toString();
                })
                .filter(Objects::nonNull).toList();
    }

    private String createErrorMsg(List<OrderDetailsIFoodDTO.ItemsIfood.Options> products, List<OrderDetailsIFoodDTO.ItemsIfood.Options> options) {
        List<String> productsNames = products == null || products.isEmpty() ? List.of() : products.stream().map(t -> t.name()).toList();
        List<String> optionsNames = options == null || options.isEmpty() ? List.of() : options.stream().map(c -> c.name()).toList();

        String toppingsPart = productsNames.isEmpty() ? "" : String.join(" / ", productsNames);
        String crustsPart = optionsNames.isEmpty() ? "" : " | Opcionais: " + String.join(" / ", optionsNames);

        return toppingsPart + crustsPart + " |*ERRO| \n Algum item ERRO no codigo PDV, verificar codigos iFood / Sistema.\n * Erro esta no(s) produto(s) faltante.";
    }

//    private void treatProductsPdvCodesNulls(List<String> productsPdvCodes, String notes) {
//       productsPdvCodes.forEach(p -> {
//           if (p == null) {
//               productsPdvCodes.remove(p);
//               notes = notes + " - foi encontrado um produto com PDV code nulo.";
//           }
//       });
//    }
//    private String determineOrderDispachTypeIFood(OrderDetailsIFoodDTO ifoodOrderDetails) {
//        if (Objects.equals(ifoodOrderDetails.orderType(), "DELIVERY")) {
//            return "delivery";
//        } else if (ifoodOrderDetails.pickup().isPresent()) {
//            return "pickup";
//        } else {
//            return "dine-in";
//        }
//    }


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
