package com.caryanam.caryanam_broker.service;

import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.PropertyFilterDto;

import java.util.List;

public interface PropertyUserService {

    List<PropertyDto> getAllProperties();

    List<PropertyDto> filterProperties(PropertyFilterDto filterDTO);

    PropertyDto getPropertyById(Long id);
}

