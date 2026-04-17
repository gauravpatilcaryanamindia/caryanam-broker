package com.caryanam.no_broker.repository;


import com.caryanam.no_broker.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Email se admin find karne ke liye (login ke time use hoga)
    Admin findByEmail(String email);

}