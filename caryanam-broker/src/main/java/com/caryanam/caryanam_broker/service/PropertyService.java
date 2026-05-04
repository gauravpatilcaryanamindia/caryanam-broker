package com.caryanam.caryanam_broker.service;


import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.PropertyFilterDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PropertyService {

    PropertyDto addProperty(PropertyDto propertyDto, Long adminId);

    List<PropertyDto> getAllProperties(Long userId, HttpServletRequest request);

    PropertyDto getPropertyById(Long id);

    PropertyDto updateProperty(Long id, PropertyDto propertyDto);

    String deleteProperty(Long id);

    String uploadPropertyImages(Long propertyId, MultipartFile[] files);

    List<PropertyDto> filterProperties(PropertyFilterDto filterDto, Long userId);
    
    List<String> getAddressesByCity(String city);

    Object getPropertiesByCityAndAddress(String city, String address);
}
