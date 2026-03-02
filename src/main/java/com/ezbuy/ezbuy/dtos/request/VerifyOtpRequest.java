package com.ezbuy.ezbuy.dtos.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank @Email
    private String email;
    
    @NotBlank
    private String otp;
}