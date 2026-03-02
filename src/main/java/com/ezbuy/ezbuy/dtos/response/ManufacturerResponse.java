package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManufacturerResponse {
    private Integer id;
    private String name;
}