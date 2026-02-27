package com.example.fuji.dto;

import java.time.LocalDateTime;

import com.example.fuji.entity.User;

import lombok.Data;

@Data
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;

    private String fullName;
    private String avatarUrl;
    private String bio;
    private String gender;
    private String phone;

    private String jlptLevel;
    private boolean active;
    private LocalDateTime createdAt;

    public static UserProfileResponse from(User user) {
        UserProfileResponse res = new UserProfileResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setBio(user.getBio());
        res.setGender(user.getGender() != null ? user.getGender().name() : null);
        res.setPhone(user.getPhone());
        res.setJlptLevel(user.getJlptLevel() != null ? user.getJlptLevel().name() : null);
        res.setActive(user.getIsActive() != null ? user.getIsActive() : false);
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }

}
