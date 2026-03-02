package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String userAvatar;
}