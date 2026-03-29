package com.example.marketplace.controller;

import com.example.marketplace.dto.AuthResponse;
import com.example.marketplace.dto.SellerRegisterRequest;
import com.example.marketplace.model.SellerApplication;
import com.example.marketplace.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {
    @Autowired
    private SellerService sellerService;

    public SellerController() {
        System.out.println("SellerController initialized");
    }

    @PostMapping("/apply")
    public ResponseEntity<SellerApplication> apply(@RequestBody SellerApplication application, Authentication auth) {
        return ResponseEntity.ok(sellerService.apply(application, auth.getName()));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<SellerApplication>> getApplications() {
        return ResponseEntity.ok(sellerService.getApplications());
    }

    @PostMapping({ "/register-shop", "/register_shop" })
    public ResponseEntity<AuthResponse> registerShop(@RequestBody SellerRegisterRequest request, Authentication auth) {
        System.out.println("Register shop endpoint hit: " + (request != null ? request.getShopName() : "null"));
        return ResponseEntity.ok(sellerService.registerShop(request, auth.getName()));
    }
}
