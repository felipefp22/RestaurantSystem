package com.RestaurantSystem.Entities.Printer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class PrintersAndCopies {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private PrintRules printRules;

    private UUID printerID;
    private Integer copies;

    //<>------------ Constructors ------------<>
    public PrintersAndCopies() {
    }
    public PrintersAndCopies(PrintRules printRules, UUID printerID, Integer copies) {
        this.printRules = printRules;
        this.printerID = printerID;
        this.copies = copies;
    }

    //<>------------ Getters and setters ------------<>
    public UUID getId() {
        return id;
    }

    public PrintRules getPrintRules() {
        return printRules;
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
