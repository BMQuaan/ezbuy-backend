package com.ezbuy.ezbuy.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;

    // private String imageUrl;

    private Integer parentId;

    private boolean isActive = true;
}