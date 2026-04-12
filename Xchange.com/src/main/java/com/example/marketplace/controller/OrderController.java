package com.example.marketplace.controller;

import com.example.marketplace.model.Order;
import com.example.marketplace.service.OrderService;
import com.example.marketplace.service.OrderTrackingService;
import com.example.marketplace.dto.OrderTrackingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderTrackingService trackingService;

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

    @PostMapping("/{orderId}/review")
    public ResponseEntity<Order> submitReview(@PathVariable String orderId, @RequestBody Map<String, Object> payload, Authentication auth) {
        Integer rating = payload.get("rating") instanceof Number ? ((Number) payload.get("rating")).intValue() : null;
        String review = (String) payload.get("review");
        return ResponseEntity.ok(orderService.submitReview(orderId, auth.getName(), rating, review));
    }

    @PutMapping("/{orderId}/review")
    public ResponseEntity<Order> editReview(@PathVariable String orderId, @RequestBody Map<String, Object> payload, Authentication auth) {
        Integer rating = payload.get("rating") instanceof Number ? ((Number) payload.get("rating")).intValue() : null;
        String comment = (String) payload.get("review");
        return ResponseEntity.ok(orderService.editReview(orderId, auth.getName(), rating, comment));
    }

    @DeleteMapping("/{orderId}/review")
    public ResponseEntity<Void> deleteReview(@PathVariable String orderId, Authentication auth) {
        orderService.deleteReview(orderId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<Order>> getMyOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrders(auth.getName()));
    }
}

