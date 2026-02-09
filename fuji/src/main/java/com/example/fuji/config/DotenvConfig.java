package com.example.fuji.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to load environment variables from .env file
 * This allows Spring Boot to access variables defined in .env using ${VAR_NAME}
 * syntax
 */
@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        try {
            // Load .env from the root directory (fuji-be/)
            Dotenv dotenv = Dotenv.configure()
                    .directory("./") // Look for .env in project root
                    .ignoreIfMissing() // Don't fail if .env doesn't exist
                    .load();

            // Set each environment variable as a system property so Spring can access it
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

            System.out.println("✅ .env file loaded successfully");
        } catch (Exception e) {
            System.err.println("⚠️  Could not load .env file: " + e.getMessage());
        }
    }
}
