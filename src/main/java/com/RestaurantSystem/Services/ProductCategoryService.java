package com.RestaurantSystem.Services;//package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.ENUMs.CustomOrderPriceRule;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.CreateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.SortPrintPriorityDTO;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.UpdateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ProductCategoryRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Collections.swap;

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
        List<ProductCategory> categories = company.getProductsCategories();

        if (categories.stream().anyMatch(cat -> cat.getPrintPriority() == null)) normalizePrintPriorities(categories);
        return company.getProductsCategories();
    }

    public List<ProductCategory> createProductCategory(String requesterID, CreateProductCategoryDTO createDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(createDTO.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        normalizePrintPriorities(company.getProductsCategories());
        company.getProductsCategories().forEach(x -> {
            if (x.getCategoryName().toLowerCase().equals(createDTO.categoryName().toLowerCase()))
                throw new RuntimeException("Category already exists");
        });

        int nextPriority = company.getProductsCategories().stream().mapToInt(ProductCategory::getPrintPriority).max().orElse(0) + 1;

        ProductCategory categoryToCreate = new ProductCategory(createDTO, company, nextPriority);
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

        company.getProductsCategories().forEach(x -> {
            if (x.getCategoryName().equalsIgnoreCase(updateDTO.categoryName()) && !x.getId().equals(categoryToUpdate.getId()))
                throw new RuntimeException("Category with name: [" + updateDTO.categoryName() + "} already exists");
        });

        categoryToUpdate.setCategoryName(updateDTO.categoryName());
        categoryToUpdate.setDescription(updateDTO.description());
        categoryToUpdate.setCustomOrderAllowed(updateDTO.customOrderAllowed());
        if (updateDTO.customOrderPriceRule() != null)
            categoryToUpdate.setCustomOrderPriceRule(CustomOrderPriceRule.valueOf(updateDTO.customOrderPriceRule()));

        productCategoryRepo.save(categoryToUpdate);
        normalizePrintPriorities(company.getProductsCategories());
        return getAllProductAndProductCategories(requesterID, updateDTO.companyID());
    }

    public List<ProductCategory> sortPrintPriority(String requesterID, SortPrintPriorityDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        List<ProductCategory> categories = company.getProductsCategories();
        normalizePrintPriorities(company.getProductsCategories());

        ProductCategory target = categories.stream()
                .filter(c -> c.getId().equals(dto.categoryID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categories.sort(Comparator.comparingInt(ProductCategory::getPrintPriority));

        int index = categories.indexOf(target);

        switch (dto.action()) {
            case "UP" -> {
                if (index > 0) {
                    swap(categories, index, index - 1);
                }
            }
            case "DOWN" -> {
                if (index < categories.size() - 1) {
                    swap(categories, index, index + 1);
                }
            }
        }

        int priority = 1;
        for (ProductCategory category : categories) {
            category.setPrintPriority(priority++);
        }
        productCategoryRepo.saveAll(categories);
        return getAllProductAndProductCategories(requesterID, dto.companyID());
    }

    // <>---------- Auxs methods ---------- <>

    private void normalizePrintPriorities(List<ProductCategory> categories) {
        if (categories == null || categories.isEmpty()) return;
        List<Integer> priorities = categories.stream()
                .map(ProductCategory::getPrintPriority)
                .toList();

        // 1️⃣ If any null → must normalize
        if (priorities.stream().anyMatch(Objects::isNull)) {
            doNormalize(categories);
            return;
        }

        List<Integer> sorted = new ArrayList<>(priorities);
        Collections.sort(sorted);

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i) != i + 1) {
                doNormalize(categories);
                return;
            }
        }
    }

    private void doNormalize(List<ProductCategory> categories) {
        categories.sort(Comparator.comparing(ProductCategory::getPrintPriority, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ProductCategory::getId));

        int priority = 1;
        for (ProductCategory category : categories) {
            category.setPrintPriority(priority++);
        }
        productCategoryRepo.saveAll(categories);
    }
}