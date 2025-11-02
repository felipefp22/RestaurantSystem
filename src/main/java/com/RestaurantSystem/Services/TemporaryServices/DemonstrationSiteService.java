package com.RestaurantSystem.Services.TemporaryServices;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CreateOrUpdateCompoundDTO;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.Company.DTOs.UpdateCompanyDTO;
import com.RestaurantSystem.Entities.Customer.DTOs.CreateOrUpdateCustomerDTO;
import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.CreateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ShiftRepo;
import com.RestaurantSystem.Services.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DemonstrationSiteService {
    private final CompaniesCompoundService companiesCompoundService;
    private final CompanyService companyService;
    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final CustomerService customerService;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final ShiftRepo shiftRepo;

    public DemonstrationSiteService(CompaniesCompoundService companiesCompoundService, CompanyService companyService, ProductService productService, ProductCategoryService productCategoryService, CustomerService customerService, AuthUserRepository authUserRepository, CompanyRepo companyRepo, ShiftRepo shiftRepo) {
        this.companiesCompoundService = companiesCompoundService;
        this.companyService = companyService;
        this.productService = productService;
        this.productCategoryService = productCategoryService;
        this.customerService = customerService;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.shiftRepo = shiftRepo;
    }

    // <>--------------- Methodos ---------------<>

    public void createACompoundAndCompany(String requesterID) {
        AuthUserLogin user = authUserRepository.findById(requesterID).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        //create demonstration compound
        CreateOrUpdateCompoundDTO createOrUpdateCompoundDTO = new CreateOrUpdateCompoundDTO(
                null,
                user.getEmail().split("@")[0] + "'s - Chain Group",
                "This is a " + user.getEmail().split("@")[0] + " chain group."
        );
        CompaniesCompound compound = companiesCompoundService.createCompaniesCompound(user.getEmail(), createOrUpdateCompoundDTO);
        user.getCompaniesCompounds().add(compound);

        //create demonstration company
        CreateCompanyDTO createCompanyDTO = new CreateCompanyDTO(
                compound.getId(),
                user.getEmail().split("@")[0] + "'s - Restaurant",
                user.getEmail(),
                "(212) 555-1234",
                "Brooksfield 1544, New York, NY 10001",
                null,
                25
        );
        Company company = companyService.createCompany(user.getEmail(), createCompanyDTO);
        user.getCompaniesCompounds().getFirst().getCompanies().add(company);

        setCompanyGeolocationAndCreateDemonstrationCustomers(user, company, 33.715831, -117.989569);

        //create demonstration products categories
        List<CreateProductCategoryDTO> productCategoriesDTOs = List.of(
                new CreateProductCategoryDTO(company.getId(), "Beverages", "Beverages"),
                new CreateProductCategoryDTO(company.getId(), "Foods", "Foods"),
                new CreateProductCategoryDTO(company.getId(), "Desserts", "Desserts"));

        Set<ProductCategory> pCategoriesCreated = new HashSet<>();
        productCategoriesDTOs.forEach(dto -> {
            var pCreated = productCategoryService.createProductCategory(user.getEmail(), dto);
            pCategoriesCreated.addAll(pCreated);
        });

        //create demonstration products
        UUID pCategoryBeveragesID = pCategoriesCreated.stream().filter(c -> c.getCategoryName().equals("Beverages")).map(ProductCategory::getId).findFirst().orElseThrow(() -> new RuntimeException("Beverages category not created"));
        UUID pCategoryFoodsID = pCategoriesCreated.stream().filter(c -> c.getCategoryName().equals("Foods")).map(ProductCategory::getId).findFirst().orElseThrow(() -> new RuntimeException("Foods category not created"));
        UUID pCategoryDessertsID = pCategoriesCreated.stream().filter(c -> c.getCategoryName().equals("Desserts")).map(ProductCategory::getId).findFirst().orElseThrow(() -> new RuntimeException("Desserts category not created"));

        List<CreateOrUpdateProductDTO> productDTOS = List.of(
                new CreateOrUpdateProductDTO(company.getId(), null, "Coca-Cola 20oz", 5.00, "Coca-Cola 20oz", "xpto.com.br", String.valueOf(pCategoryBeveragesID)),
                new CreateOrUpdateProductDTO(company.getId(), null, "Pepsi 20oz", 5.00, "Pepsi 20oz", "xpto.com.br", String.valueOf(pCategoryBeveragesID)),
                new CreateOrUpdateProductDTO(company.getId(), null, "Burger", 20.00, "Cheeseburger with fries", "xpto.com.br", String.valueOf(pCategoryFoodsID)),
                new CreateOrUpdateProductDTO(company.getId(), null, "Pizza", 30.00, "Pepperoni Pizza", "xpto.com.br", String.valueOf(pCategoryFoodsID)),
                new CreateOrUpdateProductDTO(company.getId(), null, "Tiramisù", 10.00, "Vanilla Ice Cream", "xpto.com.br", String.valueOf(pCategoryDessertsID)),
                new CreateOrUpdateProductDTO(company.getId(), null, "Cake", 15.00, "Chocolate Cake", "xpto.com.br", String.valueOf(pCategoryDessertsID))
        );

        productDTOS.forEach(dto -> {
            productService.createProduct(user.getEmail(), dto);
        });
    }

    public void setCompanyGeolocationAndCreateDemonstrationCustomers(AuthUserLogin user, Company company, Double lat, Double lng) {
        companyService.setCompanyGeoLocation(user.getEmail(), new UpdateCompanyDTO(
                company.getId(),
                null,
                null,
                null,
                null,
                lat,
                lng,
                null,
                0
        ));

        //create demonstration customers
        List<PointDTO> demoPoints = generateRandomPoints(lat, lng, 3.0, 10);
        demoPoints.addAll(List.of(demoPoints.get(0), demoPoints.get(0)));
        List<String> demoNames = List.of("Alice", "Bob", "Charlie", "David", "Eva", "Frank", "Grace", "Hannah", "Ian", "Jack", "Liam", "Mia");

        for (int i = 0; i < demoPoints.size(); i++) {
            PointDTO point = demoPoints.get(i);
            String name = demoNames.get(i);
            CreateOrUpdateCustomerDTO CreateOrUpdateCustomerDTO = new CreateOrUpdateCustomerDTO(
                    company.getId(),
                    null,
                    name,
                    "(212) 555-1234",
                    name + "Address",
                    String.valueOf(new Random().nextInt(1, 9999)),
                    "Company City",
                    "Company State",
                    "10001",
                    point.lat,
                    point.lng,
                    name + " Apt"
            );
            customerService.createCustomer(user.getEmail(), CreateOrUpdateCustomerDTO);
        }
    }


    private static List<PointDTO> generateRandomPoints(double lat, double lng, double radiusKm, int count) {
        double EARTH_RADIUS_KM = 6371.0;

        List<PointDTO> pointDTOS = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            // random distance within the radius (0–radius)
            double distance = radiusKm * Math.sqrt(random.nextDouble());

            // random bearing (0–360 degrees in radians)
            double bearing = 2 * Math.PI * random.nextDouble();

            // convert distance to angular distance
            double angularDistance = distance / EARTH_RADIUS_KM;

            double latRad = Math.toRadians(lat);
            double lngRad = Math.toRadians(lng);

            double newLat = Math.asin(Math.sin(latRad) * Math.cos(angularDistance)
                    + Math.cos(latRad) * Math.sin(angularDistance) * Math.cos(bearing));

            double newLng = lngRad + Math.atan2(
                    Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(latRad),
                    Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(newLat)
            );

            // normalize longitude (-180 to 180)
            newLng = ((newLng + 3 * Math.PI) % (2 * Math.PI)) - Math.PI;

            pointDTOS.add(new PointDTO(Math.toDegrees(newLat), Math.toDegrees(newLng)));
        }

        return pointDTOS;
    }

    public record PointDTO(double lat, double lng) {
    }
    
}
