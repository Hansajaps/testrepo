package com.example.marketplace.service;

import com.example.marketplace.dto.ProductDto;
import com.example.marketplace.model.Product;
import com.example.marketplace.model.User;
import com.example.marketplace.model.Shop;
import com.example.marketplace.repository.ProductRepository;
import com.example.marketplace.repository.ShopRepository;
import com.example.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    public ProductDto createProduct(ProductDto dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String shopName = null;
        if (user.getShopId() != null) {
            shopName = shopRepository.findById(user.getShopId()).map(Shop::getShopName).orElse(null);
        }

        Product product = Product.builder()
                .sellerId(user.getEmail())
                .shopId(user.getShopId())
                .shopName(shopName)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .stockQuantity(dto.getStockQuantity())
                .images(dto.getImages())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> getProductsBySeller(String userEmail) {
        return productRepository.findBySellerId(userEmail).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToDto(product);
    }

    public ProductDto updateProduct(String id, ProductDto dto, String userEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if the user is the owner
        if (!product.getSellerId().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to update this product");
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImages(dto.getImages());
        product.setStatus(dto.getStatus());
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);
        return mapToDto(updated);
    }

    public void deleteProduct(String id, String userEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if the user is the owner
        if (!product.getSellerId().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to delete this product");
        }

        productRepository.delete(product);
    }

    private ProductDto mapToDto(Product product) {
        String shopName = product.getShopName();
        if (shopName == null && product.getShopId() != null) {
            shopName = shopRepository.findById(product.getShopId()).map(Shop::getShopName)
                    .orElse("Shop #" + product.getShopId().substring(0, 5) + "...");
        }

        return ProductDto.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .shopId(product.getShopId())
                .shopName(shopName)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .images(product.getImages())
                .createdAt(product.getCreatedAt())
                .status(product.getStatus())
                .build();
    }
}
