package com.caryanam.caryanam_broker.dto;



import java.util.List;

import lombok.Data;


@Data
public class OwnerFacilityRequest {
    private Long ownerId;

    private List<FacilityDto> facilities;
}
