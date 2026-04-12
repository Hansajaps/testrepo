package com.example.marketplace.repository;

import com.example.marketplace.model.ContactMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContactRepository extends MongoRepository<ContactMessage, String> {
}
