package com.RestaurantSystem.Entities.Printer;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Printer.DTOs.CreateOrUpdatePrinterDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Printer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private Company company;

    private String printerCustomName;
    private String type;
    private String name;
    private String usbName;
    private String mac;
    private String ip;

    //<>------------ Constructors ------------<>
    private Printer() {
    }

    public Printer(CreateOrUpdatePrinterDTO dto, Company company) {
        this.company = company;
        this.printerCustomName = dto.printerCustomName();
        this.type = dto.type();
        this.name = dto.name();
        this.usbName = dto.usbName();
        this.mac = dto.mac();
        this.ip = dto.ip();
    }


    //<>------------ Getters and setters ------------<>
    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getPrinterCustomName() {
        return printerCustomName;
    }

    public void setPrinterCustomName(String printerCustomName) {
        this.printerCustomName = printerCustomName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsbName() {
        return usbName;
    }

    public void setUsbName(String usbName) {
        this.usbName = usbName;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
