package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.dto.ApiResponse;
import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.PropertyFilterDto;
import com.caryanam.caryanam_broker.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<PropertyDto>>> getAll() {

        List<PropertyDto> data = propertyService.getAllProperties();

        ApiResponse<List<PropertyDto>> response =
                ApiResponse.<List<PropertyDto>>builder()
                        .status("success")
                        .message("Properties fetched successfully")
                        .data(data)

                        .build();

        return ResponseEntity.ok(response);
    }


    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<List<PropertyDto>>> filter(
            @RequestBody PropertyFilterDto dto) {

        List<PropertyDto> data = propertyService.filterProperties(dto);

        ApiResponse<List<PropertyDto>> response =
                ApiResponse.<List<PropertyDto>>builder()
                        .status("success")
                        .message(data.isEmpty() ? "No properties found" : "Filtered properties fetched successfully")
                        .data(data)

                        .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyDto>> getById(@PathVariable Long id) {

        PropertyDto data = propertyService.getPropertyById(id);

        return ResponseEntity.ok(
                ApiResponse.<PropertyDto>builder()
                        .status("success")
                        .message("Property fetched successfully")
                        .data(data)

                        .build()
        );
    }
}




