package com.caryanam.caryanam_broker.dto;


import com.caryanam.caryanam_broker.Enum.BhkType;
import com.caryanam.caryanam_broker.Enum.FurnishingType;
import com.caryanam.caryanam_broker.Enum.PropertyType;
import lombok.Data;

@Data
public class PropertyDto {

    private Long id;
    private String title;
    private Double  price;
    private String location;
    private String description;
    private PropertyType propertyType;
    private String mobileNumber;
    private Integer likesCount;
    private Integer viewsCount;
    private String status;
    private BhkType bhkType;
    private FurnishingType furnishing;
    private String carpetArea;

}
