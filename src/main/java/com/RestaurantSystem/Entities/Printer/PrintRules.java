package com.RestaurantSystem.Entities.Printer;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.ENUMs.PrintCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class PrintRules {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private Company company;

    //Can not Repeat
    @Enumerated(EnumType.STRING)
    private PrintCategory printCategory;

    @OneToMany(mappedBy = "printRules", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PrintersAndCopies> printersAndCopies;

    //<>------------ Constructors ------------<>
    private PrintRules() {
    }

    public PrintRules(Company company, PrintCategory printCategory) {
        this.company = company;
        this.printCategory = printCategory;
    }

    //<>------------ Getters and setters ------------<>

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public PrintCategory getPrintCategory() {
        return printCategory;
    }

    public List<PrintersAndCopies> getPrintersAndCopies() {
        if (printersAndCopies == null) return new ArrayList<>();
        return printersAndCopies;
    }
}
