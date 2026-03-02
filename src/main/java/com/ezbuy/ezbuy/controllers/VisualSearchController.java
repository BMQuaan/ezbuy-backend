package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.VisualSearchResponse; 
import com.ezbuy.ezbuy.services.VisualSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class VisualSearchController {

    private final VisualSearchService visualSearchService;

    @PostMapping("/by-image")
    public ResponseEntity<ApiResponse<VisualSearchResponse>> searchByImage( 
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please upload an image file.");
        }
        
        VisualSearchResponse result = visualSearchService.searchByImage(file);
        
        return ResponseEntity.ok(ApiResponse.success(
                result, 
                "Visual search completed successfully."
        ));
    }
}