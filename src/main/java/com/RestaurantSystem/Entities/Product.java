package com.RestaurantSystem.Entities;

import com.RestaurantSystem.Entities.DTOs.CreateOrUpdateProductDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Setter private String name;
    @Setter private double price;
    @Setter private String description;
    @Setter private String imagePath;

    @OneToOne
    @Setter private ProductCategory category;

    // <>------------ Constructors ------------<>
    public Product(CreateOrUpdateProductDTO productToCreate, ProductCategory category) {
        this.name = productToCreate.name();
        this.price = productToCreate.price();
        this.description = productToCreate.description();
        this.category = category;
    }
}
