package com.RestaurantSystem.Services;//package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductCategoryService {

    private final CompanyRepo companyRepo;
    private final AuthUserRepository authUserRepository;

    public ProductCategoryService(CompanyRepo companyRepo, AuthUserRepository authUserRepository) {
        this.companyRepo = companyRepo;
        this.authUserRepository = authUserRepository;
    }


    // <> ---------- Methods ---------- <>
    public Set<String> getAllProductAndProductCategories(String requesterID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Set<String> response = new HashSet<>(company.getProductsCategories());

        return response;
    }

    public Set<String> createProductCategory(String requesterID, String categoryName) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product category, ask to manager");

        company.getProductsCategories().forEach(x -> {
            if (x.toLowerCase().equals(categoryName.toLowerCase()))
                throw new RuntimeException("Category already exists");
        });

        company.addProductsCategory(categoryName);

        companyRepo.save(company);
        return getAllProductAndProductCategories(requesterID);
    }

    public Set<String> updateCategory(String requesterID, String categoryNameToReplace, String newCategoryName) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product category, ask to manager");

        if (!company.getProductsCategories().contains(categoryNameToReplace)) throw new RuntimeException("Category not found");
        if (company.getProductsCategories().contains(newCategoryName))
            throw new RuntimeException("Category already exists");

        company.removeProductsCategory(categoryNameToReplace);
        company.addProductsCategory(newCategoryName);

        company.getProducts().forEach(x -> {
            if (x.getCategory().equals(categoryNameToReplace)) x.setCategory(newCategoryName);
        });

        companyRepo.save(company);

        return getAllProductAndProductCategories(requesterID);
    }
}
