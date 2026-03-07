package com.example.fuji;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FujiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FujiApplication.class, args);
	}

}
