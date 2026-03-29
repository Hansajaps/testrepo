package com.example.marketplace.service;

import com.example.marketplace.dto.UserDto;
import com.example.marketplace.model.User;
import com.example.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDto(user);
    }

    public UserDto updateProfile(String email, UserDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setProfilePhotoUrl(dto.getProfilePhotoUrl());

        return mapToDto(userRepository.save(user));
    }

    public UserDto addToWishlist(String email, String productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.getWishlist().add(productId);
        return mapToDto(userRepository.save(user));
    }

    public UserDto removeFromWishlist(String email, String productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.getWishlist().remove(productId);
        return mapToDto(userRepository.save(user));
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .roles(user.getRoles())
                .wishlist(user.getWishlist())
                .shopId(user.getShopId())
                .hasShop(user.getShopId() != null)
                .build();
    }
}
