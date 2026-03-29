package com.example.marketplace.controller;

import com.example.marketplace.dto.VehicleDto;
import com.example.marketplace.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<Page<VehicleDto>> list(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(vehicleService.search(filters, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    @PostMapping
    public ResponseEntity<VehicleDto> create(@Valid @RequestBody VehicleDto dto, Authentication auth) {
        String sellerId = auth.getName(); // email
        return ResponseEntity.ok(vehicleService.create(dto, sellerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleDto> update(@PathVariable String id, @Valid @RequestBody VehicleDto dto,
            Authentication auth) {
        String sellerId = auth.getName();
        return ResponseEntity.ok(vehicleService.update(id, dto, sellerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication auth) {
        String sellerId = auth.getName();
        vehicleService.delete(id, sellerId);
        return ResponseEntity.noContent().build();
    }
}
