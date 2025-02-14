package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.DTOs.ChangeOrderTableDTO;
import com.RestaurantSystem.Entities.DTOs.UpdateNotesOnOrderDTO;
import com.RestaurantSystem.Entities.DTOs.CreateOrderDTO;
import com.RestaurantSystem.Entities.DTOs.ProductsToAddOnOrderDTO;
import com.RestaurantSystem.Entities.Order;
import com.RestaurantSystem.Services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // <>------------ Methods ------------<>
    @GetMapping("/get-orders-by-date")
    public ResponseEntity<List<Order>> getOrdersByDate(@PathVariable LocalDateTime date) {

        var response = orderService.getOrdersByDate(date);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-order")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderDTO orderToCreate) {

        var response = orderService.createOrder(orderToCreate);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/add-notes-on-order")
    public ResponseEntity<Order> updateNotesOnOrder(@RequestBody UpdateNotesOnOrderDTO notesAndOrderID) {
        var response = orderService.addNotesOnOrder(notesAndOrderID.orderId(), notesAndOrderID.notes());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-products-on-order")
    public ResponseEntity<Order> addProductsOnOrder(@RequestBody ProductsToAddOnOrderDTO products) {
        var response = orderService.addProductsOnOrder(products);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove-products-on-order")
    public ResponseEntity<Order> removeProductsOnOrder(@RequestBody ProductsToAddOnOrderDTO products) {
        var response = orderService.removeProductsOnOrder(products);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/move-to-another-table/{orderID}/table-number-toward/{newTableNumber}")
    public ResponseEntity<Order> moveToAnotherTable(@RequestBody ChangeOrderTableDTO changeOrderTableDTO) {
        var response = orderService.moveToAnotherTable(changeOrderTableDTO.orderId(), changeOrderTableDTO.tableToGo());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/close-order/{orderID}")
    public ResponseEntity<Order> closeOrder(@PathVariable UUID orderID) {
        var response = orderService.closeOrder(orderID);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/confirm-paid-order/{orderID}")
    public ResponseEntity<Order> confirmPaidOrder(@PathVariable UUID orderID) {
        var response = orderService.confirmPaidOrder(orderID);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cancel-order/{orderID}")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderID) {
        var response = orderService.cancelOrder(orderID);

        return ResponseEntity.ok(response);
    }
}
