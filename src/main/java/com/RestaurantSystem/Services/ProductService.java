package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.Product.DTOs.FindProductDTO;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import com.RestaurantSystem.Repositories.CompanyRepo;
import com.RestaurantSystem.Repositories.ProductCategoryRepo;
import com.RestaurantSystem.Repositories.ProductRepo;
import com.RestaurantSystem.Services.AuxsServices.VerificationsServices;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepo productRepo;
    private final ProductCategoryRepo productCategoryRepo;
    private final AuthUserRepository authUserRepository;
    private final CompanyRepo companyRepo;
    private final VerificationsServices verificationsServices;

    public ProductService(ProductRepo productRepo, ProductCategoryRepo productCategoryRepo, AuthUserRepository authUserRepository, CompanyRepo companyRepo, VerificationsServices verificationsServices) {
        this.productRepo = productRepo;
        this.productCategoryRepo = productCategoryRepo;
        this.authUserRepository = authUserRepository;
        this.companyRepo = companyRepo;
        this.verificationsServices = verificationsServices;
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

//    public Product getProductById(String requesterID, FindProductDTO dto) {
//        AuthUserLogin requester = authUserRepository.findById(requesterID)
//                .orElseThrow(() -> new RuntimeException("Requester not found"));
//
//        Company company = companyRepo.findById(dto.companyID())
//                .orElseThrow(() -> new RuntimeException("Company not found"));
//
//        if (!verificationsServices.worksOnCompany(company, requester)) throw new RuntimeException("You are not allowed to see the categories of this company");
//
//        List<Product> allProducts = company.getProductsCategories().stream().flatMap(pc -> pc.getProducts().stream()).toList();
//
//        return allProducts.stream().filter(p -> p.getId().equals(dto.productID())).findFirst()
//                .orElseThrow(() -> new RuntimeException("Product not found"));
//    }

    public Product createProduct(String requesterID, CreateOrUpdateProductDTO productToCreate) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(productToCreate.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, requester)) throw new RuntimeException("You are not allowed to add a product, ask to manager or supervisor");

        ProductCategory productCategoryToAddProduct = company.getProductsCategories().stream()
                .filter(pc -> pc.getId().equals(UUID.fromString(productToCreate.productCategoryID())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product(productToCreate, productCategoryToAddProduct);

        productRepo.save(product);

        return productRepo.save(product);
    }

    public Product updateProduct(String requesterID, CreateOrUpdateProductDTO productToUpdateDTO) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(productToUpdateDTO.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, requester)) throw new RuntimeException("You are not allowed to add a product, ask to manager or supervisor");

        List<Product> allProducts = company.getProductsCategories().stream().flatMap(pc -> pc.getProducts().stream()).toList();

        Product productToUpdate = allProducts.stream().filter(p -> p.getId().equals(productToUpdateDTO.productID())).findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductCategory productCategoryToAddProduct = company.getProductsCategories().stream()
                .filter(pc -> pc.getId().equals(UUID.fromString(productToUpdateDTO.productCategoryID())))
                .findFirst()
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

    public void deleteProduct(String requesterID, FindProductDTO dto) {
        AuthUserLogin requester = authUserRepository.findById(requesterID)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Company company = companyRepo.findById(dto.companyID())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!verificationsServices.isOwnerOrManagerOrSupervisor(company, requester)) throw new RuntimeException("You are not allowed to add a product, ask to manager or supervisor");

        List<Product> allProducts = company.getProductsCategories().stream().flatMap(pc -> pc.getProducts().stream()).toList();

        Product productToDelete = allProducts.stream().filter(p -> p.getId().equals(dto.productID())).findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepo.delete(productToDelete);
    }
}
