package com.example.marketplace.service;

import com.example.marketplace.model.SellerApplication;
import com.example.marketplace.model.Shop;
import com.example.marketplace.model.User;
import com.example.marketplace.dto.SellerRegisterRequest;
import com.example.marketplace.dto.AuthResponse;
import com.example.marketplace.dto.UserDto;
import com.example.marketplace.repository.SellerApplicationRepository;
import com.example.marketplace.repository.ShopRepository;
import com.example.marketplace.repository.UserRepository;
import com.example.marketplace.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class SellerService {
        @Autowired
        private SellerApplicationRepository repository;

        @Autowired
        private ShopRepository shopRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JwtUtil jwtUtil;

        public SellerApplication apply(SellerApplication application, String userId) {
                application.setUserId(userId);
                application.setStatus("PENDING");
                application.setAppliedAt(LocalDateTime.now());
                return repository.save(application);
        }

        public List<SellerApplication> getApplications() {
                return repository.findAll();
        }

        public void updateStatus(String id, String status) {
                SellerApplication app = repository.findById(id).orElseThrow();
                app.setStatus(status);
                repository.save(app);
        }

        public AuthResponse registerShop(SellerRegisterRequest request, String email) {
                System.out.println("Starting shop registration for: " + email);

                if (email == null || email.trim().isEmpty()) {
                        throw new RuntimeException("Email is required for shop registration");
                }

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found: " + email));

                // Ensure roles is initialized and mutable
                Set<String> roles = user.getRoles();
                if (roles == null) {
                        roles = new java.util.HashSet<>();
                } else {
                        roles = new java.util.HashSet<>(roles); // Ensure mutability
                }
                roles.add("ROLE_SELLER");
                user.setRoles(roles);
                user = userRepository.save(user);

                // Create and save Shop
                Shop shop = Shop.builder()
                                .userId(user.getId())
                                .shopName(request.getShopName())
                                .shopCategories(request.getShopCategories() != null
                                                ? java.util.Arrays
                                                                .asList(request.getShopCategories().split("\\s*,\\s*"))
                                                : null)
                                .district(request.getDistrict())
                                .city(request.getCity())
                                .acceptedPaymentMethods(request.getAcceptedPaymentMethods() != null
                                                ? java.util.Arrays.asList(
                                                                request.getAcceptedPaymentMethods().split("\\s*,\\s*"))
                                                : null)
                                .build();

                shop = shopRepository.save(shop);

                // Update user's shopId reference
                user.setShopId(shop.getId());
                user = userRepository.save(user);

                System.out.println("Shop registered successfully for: " + email + ", shopId: " + shop.getId());

                // Generate new token with updated roles
                String token = jwtUtil.generateToken(user);

                return AuthResponse.builder()
                                .token(token)
                                .user(mapToDto(user))
                                .build();
        }

        private UserDto mapToDto(User user) {
                return UserDto.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .name(user.getName())
                                .address(user.getAddress())
                                .phone(user.getPhone())
                                .nicNumber(user.getNicNumber())
                                .createdAt(user.getCreatedAt())
                                .profilePhotoUrl(user.getProfilePhotoUrl())
                                .roles(user.getRoles() != null ? user.getRoles() : new java.util.HashSet<>())
                                .hasShop(user.getShopId() != null
                                                || (user.getId() != null
                                                                && shopRepository.existsByUserId(user.getId())))
                                .shopId(user.getShopId())
                                .build();
        }
}
