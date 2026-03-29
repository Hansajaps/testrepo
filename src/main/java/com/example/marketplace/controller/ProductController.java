package com.example.marketplace.controller;

import com.example.marketplace.dto.ProductDto;
import com.example.marketplace.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/seller/me")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<ProductDto>> getMyProducts(Authentication auth) {
        return ResponseEntity.ok(productService.getProductsBySeller(auth.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto, Authentication auth) {
        return ResponseEntity.ok(productService.createProduct(dto, auth.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable String id, @RequestBody ProductDto dto,
            Authentication auth) {
        return ResponseEntity.ok(productService.updateProduct(id, dto, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id, Authentication auth) {
        productService.deleteProduct(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
