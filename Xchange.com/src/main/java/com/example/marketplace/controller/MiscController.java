package com.example.marketplace.controller;

import com.example.marketplace.model.Vehicle;
import com.example.marketplace.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MiscController {
    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping("/api/stats/featured")
    public ResponseEntity<List<Vehicle>> getFeatured() {
        // Just return top 5 latest for now
        return ResponseEntity.ok(vehicleRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent());
    }

    @GetMapping("/api/search/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String q) {
        return ResponseEntity.ok(vehicleRepository.searchByTitle(q, PageRequest.of(0, 10))
                .getContent().stream()
                .map(Vehicle::getTitle)
                .collect(Collectors.toList()));
    }
}
