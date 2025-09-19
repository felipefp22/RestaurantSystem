package com.RestaurantSystem.Services;//package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.CreateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.UpdateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ProductCategoryRepo;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductCategoryService {

    private final CompanyRepo companyRepo;
    private final AuthUserRepository authUserRepository;
    private final ProductCategoryRepo productCategoryRepo;

    public ProductCategoryService(CompanyRepo companyRepo, AuthUserRepository authUserRepository, ProductCategoryRepo productCategoryRepo) {
        this.companyRepo = companyRepo;
        this.authUserRepository = authUserRepository;
        this.productCategoryRepo = productCategoryRepo;
    }


    // <> ---------- Methods ---------- <>
    public List<ProductCategory> getAllProductAndProductCategories(String requesterID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return company.getProductsCategories();
    }

    public List<ProductCategory> createProductCategory(String requesterID, CreateProductCategoryDTO createDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwnerCompound().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product category, ask to manager");

        company.getProductsCategories().forEach(x -> {
            if (x.getCategoryName().toLowerCase().equals(createDTO.categoryName().toLowerCase()))
                throw new RuntimeException("Category already exists");
        });

        ProductCategory categoryToCreate = new ProductCategory(createDTO, company);
        productCategoryRepo.save(categoryToCreate);

        return getAllProductAndProductCategories(requesterID);
    }

    public List<ProductCategory> updateCategory(String requesterID, UpdateProductCategoryDTO updateDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        ProductCategory categoryToUpdate = productCategoryRepo.findById(updateDTO.id())
                .orElseThrow(() -> new RuntimeException("Categories not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwnerCompound().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product category, ask to manager");

        company.getProductsCategories().forEach(x ->{
            if (x.getCategoryName().toLowerCase().equals(updateDTO.categoryName().toLowerCase())) throw new RuntimeException("Category already exists");
        });

        categoryToUpdate.setCategoryName(updateDTO.categoryName());
        categoryToUpdate.setDescription(updateDTO.description());

        productCategoryRepo.save(categoryToUpdate);

        return getAllProductAndProductCategories(requesterID);
    }
}
