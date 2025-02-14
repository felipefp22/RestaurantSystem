package com.RestaurantSystem.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Setter private String name;
    @Setter private double price;
    @Setter private String description;
    @Setter private String imagePath;
    @Setter private ProductCategory category;


    // <>------------ Constructors ------------<>
    public Products(String name, double price, String description, ProductCategory category) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
    }
}
