package com.RestaurantSystem.Entities.Printer;

import com.RestaurantSystem.Entities.Company.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
public class PrintSync {

    @Id
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private Company company;
    private LocalDateTime releaseDateUtc;

    @Column(columnDefinition = "TEXT")
    private String text;

    // <>------------ Constructors ------------<>
    private PrintSync() {
    }

    public PrintSync(Company company, LocalDateTime ordersItems, String text) {
        this.id = UUID.randomUUID();
        this.company = company;
        this.releaseDateUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.text = text;
    }


    // <>------------ Getters and Setters ------------<>

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public LocalDateTime getReleaseDateUtc() {
        return releaseDateUtc;
    }

    public String getText() {
        return text;
    }
}
