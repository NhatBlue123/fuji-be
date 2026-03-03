package com.example.fuji;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class FujiApplication {

	public static void main(String[] args) {
		loadEnvFile();
		SpringApplication.run(FujiApplication.class, args);

	}

	private static void loadEnvFile() {
		try {
			Path envPath = Paths.get(".env");
			if (Files.exists(envPath)) {
				Files.lines(envPath)
						.filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
						.forEach(line -> {
							String[] parts = line.split("=", 2);
							if (parts.length == 2) {
								System.setProperty(parts[0].trim(), parts[1].trim());
							}
						});
			}
		} catch (Exception ignored) {
		}
	}

}
