package com.caryanam.caryanam_broker.repository;

import com.caryanam.caryanam_broker.entity.Admin;
import com.caryanam.caryanam_broker.entity.PropertyOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PropertyOwnerRepository extends JpaRepository<PropertyOwner,Integer> {

    boolean existsByEmail(String email);
    Optional<PropertyOwner> findByEmail(String email);

}
