package com.ezbuy.ezbuy.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionRequest {
    @NotBlank(message = "Promotion code is required")
    @Size(max = 50, message = "Code must be less than 50 characters")
    private String code;

    private String description;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be positive")
    private BigDecimal discountValue; 

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private boolean isActive = true;
}