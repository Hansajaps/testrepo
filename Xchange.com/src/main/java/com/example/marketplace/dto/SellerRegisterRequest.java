package com.example.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegisterRequest {
    private String shopName;
    private String email;
    private String shopCategories;
    private String district;
    private String city;
    private String acceptedPaymentMethods;
}
