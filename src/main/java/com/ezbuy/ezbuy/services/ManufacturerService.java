package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.dtos.response.ManufacturerResponse;
import java.util.List;

public interface ManufacturerService {
    List<ManufacturerResponse> getAllManufacturers();
}