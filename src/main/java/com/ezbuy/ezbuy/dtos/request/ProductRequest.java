package com.ezbuy.ezbuy.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be non-negative")
    private BigDecimal price;

    @NotNull(message = "Quantity in stock is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private int quantityInStock;

    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    @NotNull(message = "Manufacturer ID is required")
    private Integer manufacturerId;

    private boolean isActive = true;
}