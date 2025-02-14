package com.RestaurantSystem.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
public class ProductCategory {

    @Id
    @GeneratedValue
    @Setter private String categoryName;


    // <>------------ Constructors ------------<>

    public ProductCategory(String categoryName) {
        this.categoryName = categoryName;
    }
}
