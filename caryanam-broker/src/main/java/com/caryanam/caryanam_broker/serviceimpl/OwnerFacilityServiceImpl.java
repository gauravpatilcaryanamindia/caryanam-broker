package com.caryanam.caryanam_broker.serviceimpl;

import com.caryanam.caryanam_broker.dto.OwnerFacilityRequest;
import com.caryanam.caryanam_broker.entity.OwnerFacility;
import com.caryanam.caryanam_broker.repository.OwnerFacilityRepository;
import com.caryanam.caryanam_broker.service.OwnerFacilityService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OwnerFacilityServiceImpl implements OwnerFacilityService {

    @Autowired
    private OwnerFacilityRepository repository;
    @Transactional
    @Override
    public String saveFacilities(OwnerFacilityRequest request) {

        repository.deleteByOwnerId(request.getOwnerId());

        List<OwnerFacility> facilityList = new ArrayList<>();

        for (String facility : request.getFacilities()) {

            OwnerFacility ownerFacility = new OwnerFacility();

            ownerFacility.setOwnerId(request.getOwnerId());
            ownerFacility.setFacilityName(facility);

            facilityList.add(ownerFacility);
        }

        repository.saveAll(facilityList);

        return "Facilities Saved Successfully";
    }

    @Override
    public List<String> getFacilities(Long ownerId) {

        List<OwnerFacility> facilities =
                repository.findByOwnerId(ownerId);

        return facilities.stream()
                .map(OwnerFacility::getFacilityName)
                .collect(Collectors.toList());
    }
}