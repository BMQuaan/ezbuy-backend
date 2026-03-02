package com.ezbuy.ezbuy.mappers;

import com.ezbuy.ezbuy.dtos.response.ManufacturerResponse;
import com.ezbuy.ezbuy.entities.Manufacturer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManufacturerMapper {
    ManufacturerResponse toManufacturerResponse(Manufacturer manufacturer);
}