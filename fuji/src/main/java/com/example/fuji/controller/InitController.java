package com.example.fuji.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/init")
public class InitController {

    @GetMapping
    public String init() {
        return "Fuji backend is running!!1";
    }
}
