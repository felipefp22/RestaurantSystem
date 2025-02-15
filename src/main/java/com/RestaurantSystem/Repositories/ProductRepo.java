package com.RestaurantSystem.Repositories;

import com.RestaurantSystem.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProductRepo extends JpaRepository<Product, UUID> {

    Set<Product> findAllByCategory(String category);
}
