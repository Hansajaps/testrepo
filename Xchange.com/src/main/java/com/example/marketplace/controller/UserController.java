package com.example.marketplace.controller;

import com.example.marketplace.dto.UserDto;
import com.example.marketplace.service.UserService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<java.util.List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(Authentication auth) {
        return ResponseEntity.ok(userService.getProfile(auth.getName()));
    }

    @PostMapping("/me")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UserDto userDto, Authentication auth) {
        return ResponseEntity.ok(userService.updateProfile(auth.getName(), userDto));
    }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<UserDto> addToWishlist(@PathVariable String productId, Authentication auth) {
        return ResponseEntity.ok(userService.addToWishlist(auth.getName(), productId));
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<UserDto> removeFromWishlist(@PathVariable String productId, Authentication auth) {
        return ResponseEntity.ok(userService.removeFromWishlist(auth.getName(), productId));
    }
}
