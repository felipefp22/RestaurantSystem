package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.MarkOrderPrintSyncPrintedDTO;
import com.RestaurantSystem.Entities.Order.DTOs.*;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    public OrderController(OrderService orderService, RetriveAuthInfosService retriveAuthInfosService) {
        this.orderService = orderService;
        this.retriveAuthInfosService = retriveAuthInfosService;
    }

    // <>------------ Methods ------------<>

    @PostMapping("/create-order")
    public ResponseEntity<Order> createOrder(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody CreateOrderDTO orderToCreate) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = orderService.createOrder(requesterID, orderToCreate);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/add-notes-on-order")
    public ResponseEntity<Order> updateNotesOnOrder(@RequestHeader("Authorization") String authorizationHeader,
                                                    @RequestBody UpdateNotesOnOrderDTO notesAndOrderID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = orderService.addNotesOnOrder(requesterID, notesAndOrderID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-products-on-order")
    public ResponseEntity<Order> addProductsOnOrder(@RequestHeader("Authorization") String authorizationHeader,
                                                    @RequestBody ProductsToAddOnOrderDTO productsToAdd) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = orderService.addProductsOnOrder(requesterID, productsToAdd);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/remove-products-on-order")
    public ResponseEntity<Order> removeProductsOnOrder(@RequestHeader("Authorization") String authorizationHeader,
                                                       @RequestBody ProductsToRemoveOnOrderDTO products) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = orderService.removeProductsOnOrder(requesterID, products);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-order")
    public ResponseEntity<Order> updateOrder(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody ChangeOrderTableDTO changeOrderTableDTO) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = orderService.updateOrder(requesterID, changeOrderTableDTO);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/close-order")
    public ResponseEntity closeOrder(@RequestHeader("Authorization") String authorizationHeader,
                                     @RequestBody OrderToCloseDTO orderToCloseDTO) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        orderService.closeOrder(requesterID, orderToCloseDTO);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/confirm-paid-order")
    public ResponseEntity<Order> confirmPaidOrder(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestBody FindOrderDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = orderService.confirmPaidOrder(requesterID, dto);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/reopen-order")
    public ResponseEntity reopenOrder(@RequestHeader("Authorization") String authorizationHeader,
                                      @RequestBody ReopenOrdersDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        orderService.reopenOrder(requesterID, dto);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/cancel-order")
    public ResponseEntity<Order> cancelOrder(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody ConfirmOrCancelOrderDTO confirmOrCancelOrderDTO) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = orderService.cancelOrder(requesterID, confirmOrCancelOrderDTO);

        return ResponseEntity.ok(response);
    }


    // <>------------ Prints Service ------------<>
    @PutMapping("/mark-orderPrintSync-printed")
    public ResponseEntity markOrderAsPrinted(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody MarkOrderPrintSyncPrintedDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        orderService.markOrderAsPrinted(requesterID, dto);

        return ResponseEntity.ok().build();
    }
}
