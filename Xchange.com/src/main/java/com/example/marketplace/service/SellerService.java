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
@SuppressWarnings("null")
public class SellerService {
        @Autowired
        private SellerApplicationRepository repository;

        @Autowired
        private ShopRepository shopRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JwtUtil jwtUtil;

        @Autowired
        private NotificationService notificationService;

        public SellerApplication apply(SellerApplication application, String userId) {
                // userId is actually email from auth.getName(), so lookup the actual user ID
                User user = userRepository.findByEmail(userId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + userId));
                application.setUserId(user.getId());
                application.setUserName(user.getName());
                application.setStatus("PENDING");
                application.setAppliedAt(LocalDateTime.now());
                return repository.save(application);
        }

        public List<SellerApplication> getApplications() {
                return repository.findAll();
        }

        public List<SellerApplication> getApprovedApplications() {
                return repository.findByStatus("APPROVED");
        }

        public List<SellerApplication> getPendingApplications() {
                return repository.findByStatus("PENDING");
        }

        public List<SellerApplication> getUserApplications(String userId) {
                return repository.findByUserId(userId);
        }

        public SellerApplication approveApplication(String applicationId, String adminId) {
                SellerApplication app = repository.findById(applicationId)
                        .orElseThrow(() -> new RuntimeException("Application not found"));

                // Get the user
                User user = userRepository.findById(app.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Create and save shop
                List<String> paymentMethods = app.getAcceptedPaymentMethods() != null
                        ? java.util.Arrays.asList(app.getAcceptedPaymentMethods().split(","))
                        : java.util.Collections.emptyList();
                
                Shop shop = Shop.builder()
                        .userId(user.getId())
                        .shopName(app.getShopName())
                        .shopCategories(app.getShopCategories())
                        .district(app.getDistrict())
                        .city(app.getCity())
                        .acceptedPaymentMethods(paymentMethods)
                        .build();

                shop = shopRepository.save(shop);

                // Add ROLE_SELLER to user and set shopId
                Set<String> roles = user.getRoles();
                if (roles == null) {
                        roles = new java.util.HashSet<>();
                } else {
                        roles = new java.util.HashSet<>(roles);
                }
                roles.add("ROLE_SELLER");
                user.setRoles(roles);
                user.setShopId(shop.getId());
                userRepository.save(user);

                // Update application status
                app.setStatus("APPROVED");
                app.setReviewedAt(LocalDateTime.now());
                app.setReviewedBy(adminId);
                app = repository.save(app);

                // Send notification to user
                notificationService.createNotification(
                        user.getEmail(),
                        "Seller Application Approved ✅",
                        "Congratulations! Your application to become a seller has been approved. You can now list products on our platform.",
                        "SELLER_APPLICATION_APPROVED",
                        app.getId()
                );

                return app;
        }

        public SellerApplication rejectApplication(String applicationId, String adminId, String rejectionReason) {
                SellerApplication app = repository.findById(applicationId)
                        .orElseThrow(() -> new RuntimeException("Application not found"));

                User user = userRepository.findById(app.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Update application status
                app.setStatus("REJECTED");
                app.setReviewedAt(LocalDateTime.now());
                app.setReviewedBy(adminId);
                app.setRejectionReason(rejectionReason);
                app = repository.save(app);

                // Send notification to user
                notificationService.createNotification(
                        user.getEmail(),
                        "Seller Application Declined ❌",
                        "Unfortunately, your application to become a seller has been declined. Reason: " + rejectionReason,
                        "SELLER_APPLICATION_REJECTED",
                        app.getId()
                );

                return app;
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

