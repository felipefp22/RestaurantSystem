package com.RestaurantSystem.Services.TemporaryServices;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CreateOrUpdateCompoundDTO;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.Company.DTOs.UpdateCompanyDTO;
import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.CreateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Services.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DemonstrationSiteService {
    private final CompaniesCompoundService companiesCompoundService;
    private final CompanyService companyService;
    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final CustomerService customerService;

    public DemonstrationSiteService(CompaniesCompoundService companiesCompoundService, CompanyService companyService, ProductService productService, ProductCategoryService productCategoryService, CustomerService customerService) {
        this.companiesCompoundService = companiesCompoundService;
        this.companyService = companyService;
        this.productService = productService;
        this.productCategoryService = productCategoryService;
        this.customerService = customerService;
    }

    // <>--------------- Methodos ---------------<>

    public void createACompoundAndCompany(AuthUserLogin user) {
        //create demonstration compound
        CreateOrUpdateCompoundDTO createOrUpdateCompoundDTO = new CreateOrUpdateCompoundDTO(
                user.getEmail().split("@")[0] + " - Companies",
                "This is a " + user.getEmail().split("@")[0] + " group."
        );
        CompaniesCompound compound = companiesCompoundService.createCompaniesCompound(user.getEmail(), createOrUpdateCompoundDTO);

        //create demonstration company
        CreateCompanyDTO createCompanyDTO = new CreateCompanyDTO(
                compound.getId(),
                user.getEmail().split("@")[0] + " - Company.",
                user.getEmail(),
                "(212) 555-1234",
                "Brooksfield 1544, New York, NY 10001",
                "xpto.com.br",
                25
        );
        Company company = companyService.createCompany(user.getEmail(), createCompanyDTO);

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
                new CreateOrUpdateProductDTO(company.getId(), null, "Pizza",30.00, "Pepperoni Pizza", "xpto.com.br", String.valueOf(pCategoryFoodsID)),
                new CreateOrUpdateProductDTO(company.getId(), null, "Tiramisù", 10.00,"Vanilla Ice Cream", "xpto.com.br", String.valueOf(pCategoryDessertsID)),
                new CreateOrUpdateProductDTO(company.getId(), null, "Cake", 15.00,"Chocolate Cake", "xpto.com.br", String.valueOf(pCategoryDessertsID))
        );

        productDTOS.forEach(dto -> {
            productService.createProduct(user.getEmail(), dto);
        });


        //create demonstration customer
        Double lat = 33.715831;
        Double lon = -117.989569;
        customerService.createCustomer(user.getEmail(), company.getId(), user.getEmail().split("@")[0] + " Customer", "(212) 555-1234", user.getEmail());
    }

    public boolean setCompanyGeolocationAndCreateDemonstrationCustomers(AuthUserLogin user, Company company, Double lat, Double lng){
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


    }

    public static List<double[]> generateRandomPoints(double lat, double lng, double radiusKm, int count) {
        double EARTH_RADIUS_KM = 6371.0;

        List<double[]> points = new ArrayList<>();
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

            points.add(new double[]{Math.toDegrees(newLat), Math.toDegrees(newLng)});
        }

        return points;
    }
}
