package com.example.fuji.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * EnvironmentPostProcessor to load .env file BEFORE Spring processes
 * application.properties
 * This ensures environment variables are available when Spring resolves
 * ${VAR_NAME} placeholders
 */
public class DotenvConfig implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("../")
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> dotenvProperties = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                dotenvProperties.put(entry.getKey(), entry.getValue());
            });

            // Add to Spring's environment with high priority
            environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenvProperties", dotenvProperties));

            System.out.println("✅ .env file loaded successfully with " + dotenvProperties.size() + " variables");
        } catch (Exception e) {
            System.err.println("⚠️  Could not load .env file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
