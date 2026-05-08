package com.caryanam.caryanam_broker.service;

import com.caryanam.caryanam_broker.dto.OwnerFacilityRequest;

import java.util.List;

public interface OwnerFacilityService {

    String saveFacilities(OwnerFacilityRequest request);

    List<String> getFacilities(Long ownerId);
}
