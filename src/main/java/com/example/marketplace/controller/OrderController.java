package com.example.marketplace.controller;

import com.example.marketplace.model.Order;
import com.example.marketplace.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<Order> placeSingleOrder(@RequestBody Map<String, Object> payload, Authentication auth) {
        String productId = (String) payload.get("productId");
        int quantity = payload.get("quantity") instanceof Number ? ((Number) payload.get("quantity")).intValue() : 1;
        String shippingAddress = (String) payload.get("shippingAddress");
        String buyerName = (String) payload.get("buyerName");
        String buyerPhone = (String) payload.get("buyerPhone");
        return ResponseEntity.ok(orderService.placeSingleOrder(auth.getName(), productId, quantity, shippingAddress, buyerName, buyerPhone));
    }

    @PostMapping("/{orderId}/accept")
    public ResponseEntity<Order> acceptOrder(@PathVariable String orderId, Authentication auth) {
        return ResponseEntity.ok(orderService.acceptOrder(orderId, auth.getName()));
    }

    @PostMapping("/{orderId}/decline")
    public ResponseEntity<Order> declineOrder(@PathVariable String orderId, Authentication auth) {
        return ResponseEntity.ok(orderService.declineOrder(orderId, auth.getName()));
    }

    @GetMapping("/seller")
    public ResponseEntity<List<Order>> getSellerOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getOrdersForSeller(auth.getName()));
    }

    @PostMapping("/place-from-wishlist")
    public ResponseEntity<Order> placeOrderFromWishlist(@RequestBody Map<String, String> payload, Authentication auth) {
        String shippingAddress = payload.get("shippingAddress");
        return ResponseEntity.ok(orderService.placeOrderFromWishlist(auth.getName(), shippingAddress));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Order>> getMyOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrders(auth.getName()));
    }
}
