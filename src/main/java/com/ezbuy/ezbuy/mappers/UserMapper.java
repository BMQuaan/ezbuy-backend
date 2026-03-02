package com.ezbuy.ezbuy.mappers;

import org.mapstruct.Mapper;

import com.ezbuy.ezbuy.dtos.response.UserProfileResponse;
import com.ezbuy.ezbuy.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserProfileResponse toUserProfileResponse(User user);
}