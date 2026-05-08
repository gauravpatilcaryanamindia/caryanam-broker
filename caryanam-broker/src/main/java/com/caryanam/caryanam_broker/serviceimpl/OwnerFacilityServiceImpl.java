package com.caryanam.caryanam_broker.serviceimpl;

import com.caryanam.caryanam_broker.dto.FacilityDto;
import com.caryanam.caryanam_broker.dto.OwnerFacilityRequest;
import com.caryanam.caryanam_broker.entity.OwnerFacility;
import com.caryanam.caryanam_broker.repository.OwnerFacilityRepository;
import com.caryanam.caryanam_broker.service.OwnerFacilityService;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OwnerFacilityServiceImpl
        implements OwnerFacilityService {

    @Autowired
    private OwnerFacilityRepository repository;

    @Transactional
    @Override
    public String saveFacilities(
            OwnerFacilityRequest request) {

        repository.deleteByOwnerId(
                request.getOwnerId());

        List<OwnerFacility> facilityList =
                new ArrayList<>();

        for (FacilityDto facilityDto :
                request.getFacilities()) {

            OwnerFacility ownerFacility =
                    new OwnerFacility();

            ownerFacility.setOwnerId(
                    request.getOwnerId());

            ownerFacility.setFacilityName(
                    facilityDto.getFacilityName());

            ownerFacility.setStatus(
                    facilityDto.getStatus());

            facilityList.add(ownerFacility);
        }

        repository.saveAll(facilityList);

        return "Facilities Saved Successfully";
    }

    @Override
    public List<OwnerFacility> getFacilities(
            Long ownerId) {

        return repository.findByOwnerId(ownerId);
    }
}