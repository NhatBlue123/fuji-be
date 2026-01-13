package com.example.fuji;

import org.springframework.boot.SpringApplication;

public class TestFujiApplication {

	public static void main(String[] args) {
		SpringApplication.from(FujiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
