package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CategoryDetailResponse {
    private Integer id;
    private String name;
    private String imageUrl;
    private Integer parentId;
    private String slug;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}