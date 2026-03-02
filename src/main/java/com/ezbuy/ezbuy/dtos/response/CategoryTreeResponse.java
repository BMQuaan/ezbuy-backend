package com.ezbuy.ezbuy.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeResponse {
    private Integer id;
    private String name;
    private List<CategoryTreeResponse> children;
}