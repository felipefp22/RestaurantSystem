package com.RestaurantSystem.Services;//package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.ENUMs.CustomOrderPriceRule;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.ChangeNewProductsDefaultImgDTO;
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
    public Set<ProductCategory> getAllProductAndProductCategories(String requesterID, UUID companyID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(companyID);
        verificationsServices.worksOnCompany(company, requester);
        Set<ProductCategory> categories = company.getProductsCategories();

        if (categories.stream().anyMatch(cat -> cat.getPrintPriority() == null)) normalizePrintPriorities(categories);
        return company.getProductsCategories();
    }

    public Set<ProductCategory> createProductCategory(String requesterID, CreateProductCategoryDTO createDTO) {
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

    public Set<ProductCategory> updateCategory(String requesterID, UpdateProductCategoryDTO updateDTO) {
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
        if (updateDTO.defaultImageToNewProducts() != null)
            categoryToUpdate.setDefaultImageToNewProducts(updateDTO.defaultImageToNewProducts());

        productCategoryRepo.save(categoryToUpdate);
        normalizePrintPriorities(company.getProductsCategories());
        return getAllProductAndProductCategories(requesterID, updateDTO.companyID());
    }

    public Set<ProductCategory> changeDefaultNewProductsImage(String requesterID, ChangeNewProductsDefaultImgDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        ProductCategory categoryToUpdate = company.getProductsCategories().stream()
                .filter(x -> x.getId().equals(dto.categoryID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found"));

        categoryToUpdate.setDefaultImageToNewProducts(dto.defaultImageToNewProducts());
        productCategoryRepo.save(categoryToUpdate);
        return getAllProductAndProductCategories(requesterID, company.getId());
    }

    public Set<ProductCategory> sortPrintPriority(String requesterID, SortPrintPriorityDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        Set<ProductCategory> categories = company.getProductsCategories();
        normalizePrintPriorities(company.getProductsCategories());

        ProductCategory target = categories.stream()
                .filter(c -> c.getId().equals(dto.categoryID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found"));
        List<ProductCategory> sorted = new ArrayList<>(categories.stream().sorted(Comparator.comparingInt(ProductCategory::getPrintPriority)).toList());

        int index = sorted.indexOf(target);

        switch (dto.action()) {
            case "UP" -> {
                if (index > 0) {
                    swap(sorted, index, index - 1);
                }
            }
            case "DOWN" -> {
                if (index < categories.size() - 1) {
                    swap(sorted, index, index + 1);
                }
            }
        }

        int priority = 1;
        for (ProductCategory category : sorted) {
            category.setPrintPriority(priority++);
        }
        productCategoryRepo.saveAll(sorted);
        return getAllProductAndProductCategories(requesterID, dto.companyID());
    }


    // <>---------- Auxs methods ---------- <>

    private void normalizePrintPriorities(Set<ProductCategory> categories) {
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

    private void doNormalize(Set<ProductCategory> categories) {
        List<ProductCategory> sorted = categories.stream().sorted(Comparator.comparing(ProductCategory::getPrintPriority, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ProductCategory::getId)).toList();

        int priority = 1;
        for (ProductCategory category : sorted) {
            category.setPrintPriority(priority++);
        }
        productCategoryRepo.saveAll(sorted);
    }

//    private Set<ProductCategory> sortCategoriesByPriority(Set<ProductCategory> categories) {
//        List<ProductCategory> sorted = categories.stream().sorted(Comparator.comparing(ProductCategory::getPrintPriority, Comparator.nullsLast(Integer::compareTo))
//                .thenComparing(ProductCategory::getId)).toList();
//        return new LinkedHashSet<>(sorted);
//    }
}