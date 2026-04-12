package com.example.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String name;
    private String address;
    private String phone;
    private String nicNumber;
    private LocalDateTime createdAt;
    private String profilePhotoUrl;
    private Set<String> roles;
    private boolean hasShop;
    private String shopId;
    private Set<String> wishlist;
}
