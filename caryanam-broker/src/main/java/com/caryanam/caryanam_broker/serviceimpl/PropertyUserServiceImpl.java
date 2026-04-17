package com.caryanam.caryanam_broker.serviceimpl;

import com.caryanam.caryanam_broker.Enum.PropertyType;
import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.PropertyFilterDto;
import com.caryanam.caryanam_broker.entity.Property;
import com.caryanam.caryanam_broker.exception.ResourceNotFoundException;
import com.caryanam.caryanam_broker.repository.PropertyRepository;

import com.caryanam.caryanam_broker.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PropertyUserServiceImpl implements PropertyServiceUser {

    @Autowired
    private PropertyRepository propertyRepository;

    //GET ALL PROPERTIES
    @Override
    public List<PropertyDto> getAllProperties() {

        List<Property> propertyList = propertyRepository.findAll();
        List<PropertyDto> responseList = new ArrayList<>();

        for (Property p : propertyList) {
            responseList.add(convertToDTO(p));
        }

        return responseList;
    }

    @Override
    public List<PropertyDto> filterProperties(PropertyFilterDto filter) {

        List<Property> propertyList;

        //  CASE 1: type + range
        if (filter.getPropertyType() != null && filter.getMinPrice() != null && filter.getMaxPrice() != null) {

            propertyList = propertyRepository.findByPropertyTypeAndPriceBetween(
                    PropertyType.valueOf(filter.getPropertyType()),
                    filter.getMinPrice(),
                    filter.getMaxPrice()
            );
        }

        //  CASE 2: only type
        else if (filter.getPropertyType() != null) {

            propertyList = propertyRepository.findByPropertyType(PropertyType.valueOf(filter.getPropertyType()));
        }

        //  CASE 3: only minPrice
        else if (filter.getMinPrice() != null && filter.getMaxPrice() == null) {

            propertyList = propertyRepository.findByPriceGreaterThanEqual(filter.getMinPrice());
        }

        //  CASE 4: only maxPrice
        else if (filter.getMaxPrice() != null && filter.getMinPrice() == null) {

            propertyList = propertyRepository.findByPriceLessThanEqual(filter.getMaxPrice());
        }

        // CASE 5: only price range
        else if (filter.getMinPrice() != null && filter.getMaxPrice() != null) {

            propertyList = propertyRepository.findByPriceBetween(
                    filter.getMinPrice(),
                    filter.getMaxPrice()
            );
        }

        // CASE 6: no filter
        else {
            propertyList = propertyRepository.findAll();
        }

        List<PropertyDto> responseList = new ArrayList<>();

        for (Property p : propertyList) {
            responseList.add(convertToDTO(p));
        }

        return responseList;
    }
    //Get By ID
    @Override
    public PropertyDto getPropertyById(Long id) {

        Optional<Property> optionalProperty = propertyRepository.findById(id);

        if (optionalProperty.isEmpty()) {
            throw new ResourceNotFoundException("Property not found with id: " + id);
        }

        Property property = optionalProperty.get();

        return convertToDTO(property);
    }


    //  Common method (reuse)
    private PropertyDto convertToDTO(Property p) {

        if (p == null) {
            return null;
        }

        PropertyDto dto = new PropertyDto();

        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setPropertyType(p.getPropertyType());
        dto.setPrice(p.getPrice());
        dto.setLocation(p.getLocation());

        dto.setMobileNumber(p.getMobileNumber());

        dto.setBhkType(p.getBhkType());
        dto.setCarpetArea(p.getCarpetArea());


        return dto;
    }
}