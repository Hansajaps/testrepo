package com.example.marketplace;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MarketplaceApplication {
    public static void main(String[] args) {
        // Load .env file if it exists
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        SpringApplication.run(MarketplaceApplication.class, args);
    }
}


