package com.example.marketplace.controller;

import com.example.marketplace.model.SellerApplication;
import com.example.marketplace.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private SellerService sellerService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello Admin!";
    }

    @GetMapping("/seller-applications/pending")
    public ResponseEntity<List<SellerApplication>> getPendingSellerApplications() {
        return ResponseEntity.ok(sellerService.getPendingApplications());
    }

    @GetMapping("/seller-applications")
    public ResponseEntity<List<SellerApplication>> getAllSellerApplications() {
        return ResponseEntity.ok(sellerService.getApplications());
    }

    @PostMapping("/seller-applications/{applicationId}/approve")
    public ResponseEntity<SellerApplication> approveSellerApplication(
            @PathVariable String applicationId,
            Authentication auth) {
        SellerApplication approved = sellerService.approveApplication(applicationId, auth.getName());
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/seller-applications/{applicationId}/reject")
    public ResponseEntity<SellerApplication> rejectSellerApplication(
            @PathVariable String applicationId,
            @RequestBody Map<String, String> request,
            Authentication auth) {
        String rejectionReason = request.getOrDefault("reason", "Your application does not meet our requirements");
        SellerApplication rejected = sellerService.rejectApplication(applicationId, auth.getName(), rejectionReason);
        return ResponseEntity.ok(rejected);
    }
}
