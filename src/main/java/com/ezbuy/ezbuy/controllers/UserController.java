package com.ezbuy.ezbuy.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ezbuy.ezbuy.dtos.request.ChangePasswordRequest;
import com.ezbuy.ezbuy.dtos.request.UpdateProfileRequest;
import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.UserProfileResponse;
import com.ezbuy.ezbuy.services.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse userProfile = userService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success(userProfile, "Profile fetched successfully"));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @Valid @RequestPart("profile") UpdateProfileRequest request
    ) {
        UserProfileResponse updatedProfile = userService.updateMyProfile(request, file);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Profile updated successfully"));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}