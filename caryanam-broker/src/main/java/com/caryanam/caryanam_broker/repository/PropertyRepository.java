package com.caryanam.caryanam_broker.repository;



import com.caryanam.caryanam_broker.Enum.PropertyType;
import com.caryanam.caryanam_broker.entity.Property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PropertyRepository extends JpaRepository<Property, Long> {

    int countByPropertyOwner_OwnerId(Long ownerId);
    List<Property> findByStatus(String active);

    List<Property> findByPropertyType(PropertyType propertyType);

    List<Property> findByPropertyTypeAndPriceBetween(PropertyType propertyType, Double min, Double max);

    List<Property> findByPriceBetween(Double min, Double max);

    List<Property> findByPriceGreaterThanEqual(Double minPrice);

    List<Property> findByPriceLessThanEqual(Double maxPrice);

    List<Property> findByPropertyOwner_OwnerId(Long ownerId);

    // All properties by city
    List<Property> findByCityIgnoreCase(String city);

    // All properties by city + location
    List<Property> findByCityIgnoreCaseAndAddressIgnoreCase(String city, String address);}
