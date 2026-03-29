package com.example.marketplace.service;

import com.example.marketplace.dto.LoginRequest;
import com.example.marketplace.dto.RegisterRequest;
import com.example.marketplace.dto.AuthResponse;
import com.example.marketplace.dto.UserDto;
import com.example.marketplace.exception.BadRequestException;
import com.example.marketplace.model.User;
import com.example.marketplace.repository.UserRepository;
import com.example.marketplace.repository.ShopRepository;
import com.example.marketplace.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ShopRepository shopRepository;

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        Set<String> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            roles = request.getRoles().stream()
                    .map(role -> "ROLE_" + role.toUpperCase())
                    .collect(Collectors.toSet());
        } else {
            roles.add("ROLE_BUYER"); // Default role
        }

        User user = User.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .nicNumber(request.getNicNumber())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .user(mapToDto(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

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
                .roles(user.getRoles() != null ? user.getRoles() : new HashSet<>())
                .hasShop(user.getShopId() != null
                        || (user.getId() != null && shopRepository.existsByUserId(user.getId())))
                .shopId(user.getShopId())
                .wishlist(user.getWishlist())
                .build();
    }
}
