package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class VisualSearchResponse {
    private String predictedCategoryName;
    private List<ProductResponse> products;
}