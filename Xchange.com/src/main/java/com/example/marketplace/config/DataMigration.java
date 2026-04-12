package com.example.marketplace.config;

import com.example.marketplace.model.SellerApplication;
import com.example.marketplace.model.User;
import com.example.marketplace.repository.SellerApplicationRepository;
import com.example.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * One-time migration to backfill userName field in existing seller_applications
 * This runs once on application startup if seller applications exist without userName
 */
@Component
public class DataMigration implements CommandLineRunner {

    @Autowired
    private SellerApplicationRepository sellerApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        migrateSellerApplicationUserNames();
    }

    private void migrateSellerApplicationUserNames() {
        List<SellerApplication> applications = sellerApplicationRepository.findAll();
        
        int updatedCount = 0;
        for (SellerApplication app : applications) {
            // Only update if userName is null or empty
            if (app.getUserName() == null || app.getUserName().isEmpty()) {
                User user = userRepository.findById(app.getUserId()).orElse(null);
                if (user != null && user.getName() != null) {
                    app.setUserName(user.getName());
                    sellerApplicationRepository.save(app);
                    updatedCount++;
                }
            }
        }
        
        if (updatedCount > 0) {
            System.out.println("✓ Data Migration: Updated " + updatedCount + " seller applications with userName field");
        }
    }
}
