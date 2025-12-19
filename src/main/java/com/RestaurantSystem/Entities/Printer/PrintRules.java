package com.RestaurantSystem.Entities.Printer;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.ENUMs.PrintCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

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
    private UUID printerID;
    private Integer copies;

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

    public UUID getPrinterID() {
        return printerID;
    }

    public void setPrinterID(UUID printerID) {
        this.printerID = printerID;
    }

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }
}
