package com.example.marketplace.controller;

import com.example.marketplace.model.Ad;
import com.example.marketplace.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdController {
    @Autowired
    private AdService adService;

    @GetMapping("/active")
    public ResponseEntity<List<Ad>> listActive() {
        return ResponseEntity.ok(adService.getActiveAds());
    }
}
