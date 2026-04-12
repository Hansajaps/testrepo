package com.example.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String recipientId; // email of the user
    private String senderId; // email of the sender (optional)
    private String title;
    private String message;
    private String type; // ORDER_ACCEPTED, ORDER_DECLINED, etc.
    private String relatedId; // e.g. orderId
    private boolean isRead;
    private LocalDateTime createdAt;
}