package com.example.marketplace.service;

import com.example.marketplace.dto.VehicleDto;
import com.example.marketplace.model.User;
import com.example.marketplace.model.Shop;
import com.example.marketplace.model.Vehicle;
import com.example.marketplace.repository.VehicleRepository;
import com.example.marketplace.repository.UserRepository;
import com.example.marketplace.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private CategoryService categoryService; // For category names if needed

    public Page<VehicleDto> search(Map<String, String> filters, Pageable pageable) {
        Query query = new Query().with(pageable);

        if (filters.containsKey("q") && !filters.get("q").isEmpty()) {
            query.addCriteria(Criteria.where("title").regex(filters.get("q"), "i"));
        }
        if (filters.containsKey("category") && !filters.get("category").isEmpty()) {
            query.addCriteria(Criteria.where("categoryId").is(filters.get("category")));
        }
        if (filters.containsKey("minPrice")) {
            query.addCriteria(Criteria.where("price").gte(Double.parseDouble(filters.get("minPrice"))));
        }
        if (filters.containsKey("maxPrice")) {
            query.addCriteria(Criteria.where("price").lte(Double.parseDouble(filters.get("maxPrice"))));
        }
        if (filters.containsKey("year")) {
            query.addCriteria(Criteria.where("year").is(Integer.parseInt(filters.get("year"))));
        }
        if (filters.containsKey("location") && !filters.get("location").isEmpty()) {
            query.addCriteria(Criteria.where("location").is(filters.get("location")));
        }
        if (filters.containsKey("sellerId") && !filters.get("sellerId").isEmpty()) {
            query.addCriteria(Criteria.where("sellerId").is(filters.get("sellerId")));
        }
        if (filters.containsKey("status") && !filters.get("status").isEmpty()) {
            query.addCriteria(Criteria.where("status").is(filters.get("status")));
        } else {
            query.addCriteria(Criteria.where("status").is("ACTIVE"));
        }

        List<Vehicle> vehicles = mongoTemplate.find(query, Vehicle.class);
        long count = mongoTemplate.count(query.skip(-1).limit(-1), Vehicle.class);

        Page<Vehicle> page = PageableExecutionUtils.getPage(vehicles, pageable, () -> count);
        return page.map(this::mapToDto);
    }

    public VehicleDto getById(String id) {
        return vehicleRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    public VehicleDto create(VehicleDto dto, String sellerId) {
        User user = userRepository.findByEmail(sellerId).orElse(null);
        String shopName = null;
        if (user != null && user.getShopId() != null) {
            shopName = shopRepository.findById(user.getShopId()).map(Shop::getShopName).orElse(null);
        }

        Vehicle vehicle = mapToEntity(dto);
        vehicle.setSellerId(sellerId);
        vehicle.setShopName(shopName);
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setStatus("ACTIVE"); // Default
        return mapToDto(vehicleRepository.save(vehicle));
    }

    public VehicleDto update(String id, VehicleDto dto, String sellerId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Not authorized to update this vehicle");
        }

        vehicle.setTitle(dto.getTitle());
        vehicle.setDescription(dto.getDescription());
        vehicle.setPrice(dto.getPrice());
        vehicle.setLocation(dto.getLocation());
        vehicle.setImages(dto.getImages());
        vehicle.setFeatures(dto.getFeatures());
        vehicle.setKms(dto.getKms());
        vehicle.setYear(dto.getYear());
        vehicle.setCategoryId(dto.getCategoryId());
        vehicle.setStatus(dto.getStatus());

        return mapToDto(vehicleRepository.save(vehicle));
    }

    public void delete(String id, String sellerId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Not authorized to delete this vehicle");
        }
        vehicleRepository.deleteById(id);
    }

    private VehicleDto mapToDto(Vehicle entity) {
        String shopName = entity.getShopName();
        if (shopName == null && entity.getSellerId() != null) {
            // Fallback: look up user and then shop
            User user = userRepository.findByEmail(entity.getSellerId()).orElse(null);
            if (user != null && user.getShopId() != null) {
                shopName = shopRepository.findById(user.getShopId()).map(Shop::getShopName).orElse(null);
            }
        }

        return VehicleDto.builder()
                .id(entity.getId())
                .sellerId(entity.getSellerId())
                .shopName(shopName != null ? shopName : "Direct Seller")
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .categoryId(entity.getCategoryId())
                .location(entity.getLocation())
                .images(entity.getImages())
                .features(entity.getFeatures())
                .kms(entity.getKms())
                .year(entity.getYear())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .build();
    }

    private Vehicle mapToEntity(VehicleDto dto) {
        return Vehicle.builder()
                .id(dto.getId())
                .sellerId(dto.getSellerId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .categoryId(dto.getCategoryId())
                .location(dto.getLocation())
                .images(dto.getImages())
                .features(dto.getFeatures())
                .kms(dto.getKms())
                .year(dto.getYear())
                .createdAt(dto.getCreatedAt())
                .status(dto.getStatus())
                .build();
    }
}
