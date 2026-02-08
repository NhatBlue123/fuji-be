package com.example.fuji.service;

import com.example.fuji.dto.UpdateProfileRequest;
import com.example.fuji.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile(String username);
    UserProfileResponse updateMyProfile(String username, UpdateProfileRequest request);
}
