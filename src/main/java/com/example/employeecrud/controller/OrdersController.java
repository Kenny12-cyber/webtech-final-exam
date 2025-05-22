package com.example.employeecrud.controller;

import com.example.employeecrud.errorhandling.RequestException;
import com.example.employeecrud.model.Order;
import com.example.employeecrud.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/history")
    public ResponseEntity<?> viewOrders(Principal principal) {
        List<Order> orders = orderService.getUserOrders(principal.getName());
        return ResponseEntity.ok(orders);
    }

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<?> handleRequestException(RequestException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        return ResponseEntity.status(500).body("An unexpected error occurred. Please try again later.");
    }
}
