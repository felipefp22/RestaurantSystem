package com.RestaurantSystem.Entities.Printer;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.ENUMs.PrintCategory;
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

    @Enumerated(EnumType.STRING)
    private PrintCategory printCategory;

    @Column(columnDefinition = "TEXT")
    private String text;

    // <>------------ Constructors ------------<>
    private PrintSync() {
    }

    public PrintSync(Company company, PrintCategory printCategory, String text) {
        this.id = UUID.randomUUID();
        this.company = company;
        this.releaseDateUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.printCategory = printCategory;
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

    public PrintCategory getPrintCategory() {
        return printCategory;
    }

    public String getText() {
        return text;
    }
}
