package com.caryanam.no_broker.repository;


import com.caryanam.no_broker.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PropertyRepository extends JpaRepository<Property, Long> {

    int countByAdminId(Long adminId);

    List<Property> findByStatus(String active);
}
