package com.caryanam.caryanam_broker.repository;



import com.caryanam.caryanam_broker.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

    PropertyImage findByImageName(String imageName);
    int countByPropertyId(Long propertyId);
    List<PropertyImage> findByPropertyId(Long propertyId);
}