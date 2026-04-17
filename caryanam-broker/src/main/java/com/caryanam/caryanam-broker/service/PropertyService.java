package com.caryanam.no_broker.service;

import com.caryanam.no_broker.dto.PropertyDto;
import com.caryanam.no_broker.dto.PropertyFilterDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PropertyService {

    PropertyDto addProperty(PropertyDto propertyDto, Long adminId);

    List<PropertyDto> getAllProperties();

    PropertyDto getPropertyById(Long id);

    PropertyDto updateProperty(Long id, PropertyDto propertyDto);

    String deleteProperty(Long id);

    String uploadPropertyImages(Long propertyId, MultipartFile[] files);

    List<PropertyDto> filterProperties(PropertyFilterDto filterDto);
}
