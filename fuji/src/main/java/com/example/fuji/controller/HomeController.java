package com.example.fuji.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class HomeController {
    
    @RequestMapping
    public String home() {
        return "Welcome to the Fuji API!";
    }
}
