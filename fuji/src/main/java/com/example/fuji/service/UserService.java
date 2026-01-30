package com.example.fuji.service;

import com.example.fuji.dto.UpdateProfileRequest;
import com.example.fuji.dto.UserProfileResponse;

public interface UserService {

    UserProfileResponse getMyProfile(Long userId);

    UserProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request);
}
