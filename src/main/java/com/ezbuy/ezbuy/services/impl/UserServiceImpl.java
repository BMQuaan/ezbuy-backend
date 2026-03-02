package com.ezbuy.ezbuy.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ezbuy.ezbuy.dtos.request.ChangePasswordRequest;
import com.ezbuy.ezbuy.dtos.request.UpdateProfileRequest;
import com.ezbuy.ezbuy.dtos.response.UserProfileResponse;
import com.ezbuy.ezbuy.entities.User;
import com.ezbuy.ezbuy.mappers.UserMapper;
import com.ezbuy.ezbuy.repositories.UserRepository;
import com.ezbuy.ezbuy.services.CloudinaryService;
import com.ezbuy.ezbuy.services.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public UserProfileResponse getMyProfile() {
        User currentUser = getCurrentUser();
        return userMapper.toUserProfileResponse(currentUser);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateProfileRequest request, MultipartFile file) {
        User currentUser = getCurrentUser();
        
        currentUser.setFirstName(request.getFirstName());
        currentUser.setLastName(request.getLastName());
        currentUser.setPhone(request.getPhone());
        currentUser.setAddress(request.getAddress());

        if (file != null && !file.isEmpty()) {
            if (currentUser.getUserAvatar() != null && !currentUser.getUserAvatar().isEmpty()) {
                cloudinaryService.deleteImage(currentUser.getUserAvatar());
            }
            
            String newAvatarUrl = cloudinaryService.uploadFile(file, "avatars");
            currentUser.setUserAvatar(newAvatarUrl);
        }

        User updatedUser = userRepository.save(currentUser);
        return userMapper.toUserProfileResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User currentUser = getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new IllegalStateException("Incorrect old password.");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }
}