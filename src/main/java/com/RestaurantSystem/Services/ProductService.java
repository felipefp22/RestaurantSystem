package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ProductRepo;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepo productRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;

    public ProductService(ProductRepo productRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo) {
        this.productRepo = productRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
    }

    // <> ---------- Methods ---------- <>
    public List<Product> getAllProducts(String requesterID) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return company.getProducts();
    }

    public List<Product> getProductsByCategory(String requesterID, String category) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));


        return company.getProducts().stream().filter(x -> x.getCategory().equals(category)).toList();
    }

    public Product getProductById(String requesterID, UUID productId) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return company.getProducts().stream().filter(x -> x.getId().equals(productId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(String requesterID, CreateOrUpdateProductDTO productToCreate) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product, ask to manager");
        if (!company.getProductsCategories().contains(productToCreate.category()))
            throw new RuntimeException("Category not found, create it first");

        Product product = new Product(productToCreate, company, productToCreate.category());

        return productRepo.save(product);
    }

    public Product updateProduct(String requesterID, CreateOrUpdateProductDTO productToUpdateDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(UUID.fromString(requester.getCompanyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!company.getManagers().contains(requesterID) && !company.getOwner().equals(requesterID))
            throw new RuntimeException("You are not allowed to add a product, ask to manager");

        Product productToUpdate = company.getProducts().stream()
                .filter(x -> x.getId().equals(productToUpdateDTO.id()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productToUpdate.setName(productToUpdateDTO.name());
        productToUpdate.setPrice(productToUpdateDTO.price());
        productToUpdate.setDescription(productToUpdateDTO.description());
        productToUpdate.setImagePath(productToUpdateDTO.imagePath());

        if (productToUpdateDTO.category() != productToUpdateDTO.category()) {
            if (!company.getProductsCategories().contains(productToUpdateDTO.category()))
                throw new RuntimeException("Category not found, create it first");

            productToUpdate.setCategory(productToUpdateDTO.category());
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

        Product productToDelete = company.getProducts().stream()
                .filter(x -> x.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepo.delete(productToDelete);
    }
}
