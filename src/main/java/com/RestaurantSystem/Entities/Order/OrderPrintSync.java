package com.RestaurantSystem.Entities.Order;

import com.RestaurantSystem.Entities.Order.DTOs.OrdersItemsPrintSyncDTO;
import com.RestaurantSystem.Entities.Order.DTOs.UpdateNotesOnOrderDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Entity
public class OrderPrintSync {

    @Id
    private UUID id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    private LocalDateTime releaseDateUtc;

    private Boolean alreadyPrinted;

    private String addOrCancel;

    @Column(columnDefinition = "TEXT")
    private String items;

    // <>------------ Constructors ------------<>
    private OrderPrintSync() {
    }

    public OrderPrintSync(Order order, List<OrdersItems> ordersItems, String addOrCancel) {
        ObjectMapper mapper = new ObjectMapper();

        this.id = UUID.randomUUID();
        this.order = order;
        this.releaseDateUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.alreadyPrinted = false;
        this.addOrCancel = addOrCancel;

        List<OrdersItemsPrintSyncDTO> itemsToSet = ordersItems.stream()
                .map(OrdersItemsPrintSyncDTO::new).toList();

        try {
            this.items = mapper.writeValueAsString(itemsToSet);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing order items for print sync", e);
        }
    }


    // <>------------ Getters and Setters ------------<>

    public UUID getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public LocalDateTime getReleaseDateUtc() {
        return releaseDateUtc;
    }

    public Boolean getAlreadyPrinted() {
        return alreadyPrinted;
    }

    public void setAlreadyPrinted(Boolean alreadyPrinted) {
        this.alreadyPrinted = alreadyPrinted;
    }

    public String getAddOrCancel() {
        return addOrCancel;
    }

    public List<OrdersItemsPrintSyncDTO> getItems() {
        ObjectMapper mapper = new ObjectMapper();
        List<OrdersItemsPrintSyncDTO> items;

        try {
            items = mapper.readValue(this.items, new TypeReference<List<OrdersItemsPrintSyncDTO>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing order items for print sync", e);
        }

        return items;
    }
}
