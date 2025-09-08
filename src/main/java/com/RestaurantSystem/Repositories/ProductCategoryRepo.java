package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepo extends JpaRepository<ProductCategory, UUID> {

    Optional<List<ProductCategory>> findByCompanyId(UUID companyId);
}
