package com.tracksuit.backend.controller;

import com.tracksuit.backend.dto.ApiResponse;
import com.tracksuit.backend.dto.OrderDTO;
import com.tracksuit.backend.model.Order;
import com.tracksuit.backend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        log.debug("GET /api/v1/orders");
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("Fetched " + orders.size() + " orders", orders));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> saveOrder(@RequestBody OrderDTO orderDTO) {
        log.debug("POST /api/v1/orders - {}", orderDTO);

        if (orderDTO.getPlatform() == null || orderDTO.getPlatform().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Platform is required"));
        }
        if (orderDTO.getProductName() == null || orderDTO.getProductName().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Product name is required"));
        }

        Order saved = orderService.saveOrder(orderDTO);
        if (saved == null) {
            return ResponseEntity.ok(ApiResponse.success("Duplicate order — already exists", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Order saved successfully", saved));
    }
}
