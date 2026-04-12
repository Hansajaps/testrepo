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
import java.util.Map;

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

    @GetMapping("/applications/pending")
    public ResponseEntity<List<SellerApplication>> getPendingApplications() {
        return ResponseEntity.ok(sellerService.getPendingApplications());
    }

    @GetMapping("/applications/approved")
    public ResponseEntity<List<SellerApplication>> getApprovedApplications() {
        return ResponseEntity.ok(sellerService.getApprovedApplications());
    }

    @PostMapping({ "/register-shop", "/register_shop" })
    public ResponseEntity<AuthResponse> registerShop(@RequestBody SellerRegisterRequest request, Authentication auth) {
        System.out.println("Register shop endpoint hit: " + (request != null ? request.getShopName() : "null"));
        return ResponseEntity.ok(sellerService.registerShop(request, auth.getName()));
    }

    @PostMapping("/applications/{applicationId}/approve")
    public ResponseEntity<SellerApplication> approveApplication(
            @PathVariable String applicationId,
            Authentication auth) {
        SellerApplication approved = sellerService.approveApplication(applicationId, auth.getName());
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<SellerApplication> rejectApplication(
            @PathVariable String applicationId,
            @RequestBody Map<String, String> request,
            Authentication auth) {
        String rejectionReason = request.getOrDefault("reason", "Your application does not meet our requirements");
        SellerApplication rejected = sellerService.rejectApplication(applicationId, auth.getName(), rejectionReason);
        return ResponseEntity.ok(rejected);
    }

    @GetMapping("/application/user")
    public ResponseEntity<SellerApplication> getUserApplication(Authentication auth) {
        List<SellerApplication> applications = sellerService.getUserApplications(auth.getName());
        if (applications.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Return the most recent application
        return ResponseEntity.ok(applications.get(0));
    }
}

