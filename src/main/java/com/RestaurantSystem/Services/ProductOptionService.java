package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductOptionDTO;
import com.RestaurantSystem.Entities.Product.DTOs.FindProductOptionDTO;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Product.ProductOption;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.AddOrRemoveProductOptToProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.*;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class ProductOptionService {
    private final ProductOptionRepo productOptionRepo;
    private final VerificationsServices verificationsServices;

    public ProductOptionService(ProductOptionRepo productOptionRepo, VerificationsServices verificationsServices) {
        this.productOptionRepo = productOptionRepo;
        this.verificationsServices = verificationsServices;
    }

    // <> ---------- Methods ---------- <>
    public ProductOption createProductOption(String requesterID, CreateOrUpdateProductOptionDTO productOptToCreate) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(productOptToCreate.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

//        ProductCategory productCategoryToAddProductOpt = company.getProductsCategories().stream()
//                .filter(pc -> pc.getId().equals(UUID.fromString(productOptToCreate.productCategoryID())))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Category not found"));

        ProductOption productOpt = new ProductOption(company, productOptToCreate);
        productOptionRepo.save(productOpt);
        productOpt.setIfoodCode(productOpt.getId().toString()); //default code before validation, DO NOT REMOVE
        productOpt.setIfoodCode(validateNewIfoodCodeProductOption(company, productOpt, productOptToCreate.ifoodCode()));

        return productOptionRepo.save(productOpt);
    }

    public ProductOption addOrRemoveProductOptToProductCategory(String requesterID, AddOrRemoveProductOptToProductCategoryDTO dto){
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        Set<ProductOption> allProductOpts = company.getProductOptions();

        ProductOption productOptToAddOrRemove = allProductOpts.stream().filter(p -> p.getId().equals(dto.productOptID())).findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));
        ProductCategory productCategoryToAddProduct = company.getProductsCategories().stream()
                .filter(pc -> pc.getId().equals(dto.productCategoryID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if(dto.action().equals("add")){
            productOptToAddOrRemove.getProductCategories().add(productCategoryToAddProduct);
            productCategoryToAddProduct.getProductOptions().add(productOptToAddOrRemove);
            return productOptionRepo.save(productOptToAddOrRemove);
        } else if (dto.action().equals("remove")) {
            productOptToAddOrRemove.getProductCategories().remove(productCategoryToAddProduct);
            productCategoryToAddProduct.getProductOptions().remove(productOptToAddOrRemove);
            return productOptionRepo.save(productOptToAddOrRemove);
        } else {
            throw new RuntimeException("Action not recognized, use 'add' or 'remove'");
        }
    }

    public ProductOption updateProductOption(String requesterID, CreateOrUpdateProductOptionDTO productToUpdateDTO) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(productToUpdateDTO.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        Set<ProductOption> allProductOpts = company.getProductOptions();

        ProductOption productOptToUpdate = allProductOpts.stream().filter(p -> p.getId().equals(productToUpdateDTO.productOptID())).findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));

//        ProductCategory productCategoryToAddProduct = company.getProductsCategories().stream()
//                .filter(pc -> pc.getId().equals(UUID.fromString(productToUpdateDTO.productCategoryID())))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Category not found"));

        productOptToUpdate.setName(productToUpdateDTO.name());
        productOptToUpdate.setPrice(productToUpdateDTO.price());
        productOptToUpdate.setDescription(productToUpdateDTO.description());
        productOptToUpdate.setImagePath(productToUpdateDTO.imagePath());
        productOptToUpdate.setIfoodCode(validateNewIfoodCodeProductOption(company, productOptToUpdate, productToUpdateDTO.ifoodCode()));

//        if (productOptToUpdate.getProductCategories() != productCategoryToAddProduct) {
//            if (!company.getProductsCategories().contains(productCategoryToAddProduct))
//                throw new RuntimeException("Category not found, create it first");
//            productOptToUpdate.setProductCategories(productCategoryToAddProduct);
//        }

        return productOptionRepo.save(productOptToUpdate);
    }

    public void deleteProductOption(String requesterID, FindProductOptionDTO dto) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrManagerOrSupervisor(company, requester);

        List<ProductOption> allProductOpts = company.getProductsCategories().stream().flatMap(pc -> pc.getProductOptions().stream()).toList();

        ProductOption productOptToDelete = allProductOpts.stream().filter(p -> p.getId().equals(dto.productOptID())).findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productOptionRepo.delete(productOptToDelete);
    }

    // <> ---------- Helpers ---------- <>
    private String validateNewIfoodCodeProductOption(Company company, ProductOption productOption, String newIfoodCode) {
        if (Objects.equals(productOption.getIfoodCode(), newIfoodCode) || newIfoodCode == null) return productOption.getIfoodCode();
        if (newIfoodCode.equals("default")) return productOption.getId().toString();

        List<String> usedCodes = new ArrayList<>(company.getProductsCategories().stream()
                .flatMap(pc -> Stream.concat(
                        pc.getProducts().stream().map(Product::getIfoodCode),
                        pc.getProductOptions().stream().map(ProductOption::getIfoodCode)
                ))
                .toList());

        usedCodes.add("default");

        if (usedCodes.contains(newIfoodCode)) throw new RuntimeException("iFood code already in use");
        return newIfoodCode;
    }

}
