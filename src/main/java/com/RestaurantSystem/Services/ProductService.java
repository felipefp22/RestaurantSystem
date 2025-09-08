package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ProductCategoryRepo;
import com.RestaurantSystem.Repositories.ProductRepo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepo productRepo;
    private final ProductCategoryRepo productCategoryRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;

    public ProductService(ProductRepo productRepo, ProductCategoryRepo productCategoryRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo) {
        this.productRepo = productRepo;
        this.productCategoryRepo = productCategoryRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
    }

    // <> ---------- Methods ---------- <>
//    public List<Product> getAllProducts(String requesterID) {
//        AuthUserLogin requester = authUserRepository.findById(requesterID)
//                .orElseThrow(() -> new RuntimeException("Requester not found"));
//
//        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
//                .orElseThrow(() -> new RuntimeException("Company not found"));
//
//        return company.getProducts();
//    }

//    public List<Product> getProductsByCategory(String requesterID, String category) {
//        AuthUserLogin requester = authUserRepository.findById(requesterID)
//                .orElseThrow(() -> new RuntimeException("Requester not found"));
//
//        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
//                .orElseThrow(() -> new RuntimeException("Company not found"));
//
//
//        return company.getProducts().stream().filter(x -> x.getCategory().equals(category)).toList();
//    }

    public Product getProductById(String requesterID, UUID productId) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        return productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(String requesterID, CreateOrUpdateProductDTO productToCreate) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        ProductCategory productCategoryToAddProduct = productCategoryRepo.findById(UUID.fromString(productToCreate.productCategoryID()))
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product, ask to manager");

        Product product = new Product(productToCreate, productCategoryToAddProduct);

        productRepo.save(product);

        return productRepo.save(product);
    }

    public Product updateProduct(String requesterID, CreateOrUpdateProductDTO productToUpdateDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product, ask to manager");

        Product productToUpdate = productRepo.findById(productToUpdateDTO.id())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        ProductCategory productCategoryToAddProduct = productCategoryRepo.findById(UUID.fromString(productToUpdateDTO.productCategoryID()))
                .orElseThrow(() -> new RuntimeException("Category not found"));

        productToUpdate.setName(productToUpdateDTO.name());
        productToUpdate.setPrice(productToUpdateDTO.price());
        productToUpdate.setDescription(productToUpdateDTO.description());
        productToUpdate.setImagePath(productToUpdateDTO.imagePath());

        if (productToUpdate.getProductCategory() != productCategoryToAddProduct) {
            if (!company.getProductsCategories().contains(productCategoryToAddProduct))
                throw new RuntimeException("Category not found, create it first");
            productToUpdate.setProductCategory(productCategoryToAddProduct);
        }

        return productRepo.save(productToUpdate);
    }

    public void deleteProduct(String requesterID, UUID productId) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product, ask to manager");

        Product productToDelete = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (productToDelete.getProductCategory().getCompany().getId() != company.getId())
            throw new RuntimeException("This product does not belong to your company");

        productRepo.delete(productToDelete);
    }
}
