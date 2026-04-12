package com.example.marketplace.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB Configuration Class
 * 
 * Specifically tuned for MongoDB Atlas (M0/M10 Free/Shared tiers).
 * Addresses "Connection reset by peer" by recycling idle connections early.
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsCustomizer() {
        return builder -> builder
                // Connection Pool Settings
                .applyToConnectionPoolSettings(pool -> pool
                        .maxSize(20) // Safe limit for Atlas M0 (max 100)
                        .minSize(5)
                        .maxWaitTime(5, TimeUnit.SECONDS)
                        // Atlas or local network firewalls kill idle connections. Max aggressive recycle.
                        .maxConnectionIdleTime(4, TimeUnit.MINUTES)
                        // Force recycle connections every 30 minutes to ensure freshness
                        .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                )
                // Socket & Timeout Settings
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS) // Buffer for 9.3s RTT spikes
                )
                // Cluster & Selection Settings
                .applyToClusterSettings(cluster -> cluster
                        .serverSelectionTimeout(10, TimeUnit.SECONDS)
                )
                // Proactive Health Monitoring
                .applyToServerSettings(server -> server
                        .heartbeatFrequency(15, TimeUnit.SECONDS) // Keep alive interval
                        .minHeartbeatFrequency(500, TimeUnit.MILLISECONDS)
                )
                .readPreference(ReadPreference.primaryPreferred())
                .writeConcern(WriteConcern.MAJORITY);
    }
}
