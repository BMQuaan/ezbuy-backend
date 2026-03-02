package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.ManufacturerResponse;
import com.ezbuy.ezbuy.services.ManufacturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manufacturers")
@RequiredArgsConstructor
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ManufacturerResponse>>> getAllManufacturers() {
        List<ManufacturerResponse> manufacturers = manufacturerService.getAllManufacturers();
        return ResponseEntity.ok(ApiResponse.success(manufacturers, "Manufacturers fetched successfully"));
    }
}