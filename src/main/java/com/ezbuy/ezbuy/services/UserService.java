package com.ezbuy.ezbuy.services;

import org.springframework.web.multipart.MultipartFile;

import com.ezbuy.ezbuy.dtos.request.ChangePasswordRequest;
import com.ezbuy.ezbuy.dtos.request.UpdateProfileRequest;
import com.ezbuy.ezbuy.dtos.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile();
    UserProfileResponse updateMyProfile(UpdateProfileRequest request, MultipartFile file);
    void changePassword(ChangePasswordRequest request);
}