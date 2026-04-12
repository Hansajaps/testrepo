package com.example.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shops")
public class Shop {
    @Id
    private String id;
    private String userId;
    private String shopName;
    private List<String> shopCategories;
    private String district;
    private String city;
    private List<String> acceptedPaymentMethods;
}
