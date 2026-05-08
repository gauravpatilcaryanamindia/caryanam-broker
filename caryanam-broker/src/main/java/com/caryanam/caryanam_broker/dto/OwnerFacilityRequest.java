package com.caryanam.caryanam_broker.dto;

import java.util.List;

public class OwnerFacilityRequest {

    private Long ownerId;

    private List<String> facilities;

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }
}
