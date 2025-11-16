package com.RestaurantSystem.Services;//package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.CreateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.UpdateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ProductCategoryRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductCategoryService {

    private final CompanyRepo companyRepo;
    private final AuthUserRepository authUserRepository;
    private final ProductCategoryRepo productCategoryRepo;
    private final VerificationsServices verificationsServices;

    public ProductCategoryService(CompanyRepo companyRepo, AuthUserRepository authUserRepository, ProductCategoryRepo productCategoryRepo, VerificationsServices verificationsServices) {
        this.companyRepo = companyRepo;
        this.authUserRepository = authUserRepository;
        this.productCategoryRepo = productCategoryRepo;
        this.verificationsServices = verificationsServices;
    }


    // <> ---------- Methods ---------- <>
    public List<ProductCategory> getAllProductAndProductCategories(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.worksOnCompany(company, requester);

        return company.getProductsCategories();
    }

    public List<ProductCategory> createProductCategory(String requesterID, CreateProductCategoryDTO createDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(createDTO.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        company.getProductsCategories().forEach(x -> {
            if (x.getCategoryName().toLowerCase().equals(createDTO.categoryName().toLowerCase()))
                throw new RuntimeException("Category already exists");
        });

        ProductCategory categoryToCreate = new ProductCategory(createDTO, company);
        productCategoryRepo.save(categoryToCreate);
        company.getProductsCategories().add(categoryToCreate);
        return getAllProductAndProductCategories(requesterID, createDTO.companyID());
    }

    public List<ProductCategory> updateCategory(String requesterID, UpdateProductCategoryDTO updateDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(updateDTO.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);


        ProductCategory categoryToUpdate = company.getProductsCategories().stream()
                .filter(x -> x.getId().equals(updateDTO.categoryID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found"));

        company.getProductsCategories().forEach(x ->{
            if (x.getCategoryName().equalsIgnoreCase(updateDTO.categoryName())) throw new RuntimeException("Category with name: [" + updateDTO.categoryName() +"} already exists");
        });

        categoryToUpdate.setCategoryName(updateDTO.categoryName());
        categoryToUpdate.setDescription(updateDTO.description());

        productCategoryRepo.save(categoryToUpdate);

        return getAllProductAndProductCategories(requesterID, updateDTO.companyID());
    }
}
