package com.caryanam.caryanam_broker.repository;

import com.caryanam.caryanam_broker.entity.OwnerFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnerFacilityRepository extends JpaRepository<OwnerFacility, Long> {

    List<OwnerFacility> findByOwnerId(Long ownerId);

    void deleteByOwnerId(Long ownerId);
}