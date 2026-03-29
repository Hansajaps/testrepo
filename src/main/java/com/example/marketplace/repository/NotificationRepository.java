package com.example.marketplace.repository;

import com.example.marketplace.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String email);
    long countByRecipientIdAndIsRead(String email, boolean isRead);
}
