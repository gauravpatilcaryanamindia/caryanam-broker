package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.entity.PropertyOwner;
import com.caryanam.caryanam_broker.entity.User;
import com.caryanam.caryanam_broker.repository.PropertyOwnerRepository;
import com.caryanam.caryanam_broker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/admin")
public class adminController {

    @Autowired
    private PropertyOwnerRepository propertyOwnerRepository;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/pending-users")
    public List<User> getPendingUsers() {
        return userRepository.findByPremiumStatus("PENDING");
    }

    @GetMapping("/pending-admins")
    public List<PropertyOwner> getPendingAdmins() {
        return propertyOwnerRepository.findByPremiumStatus("PENDING");
    }

    @PostMapping("/approveOwnerPremium/{ownerId}")
    public ResponseEntity<?> approveOwner(@PathVariable Long ownerId) {
        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            return ResponseEntity.badRequest().body("Owner not found");
        }
        owner.setPremiumStatus("APPROVED");
        owner.setPremiumActive(true);
        propertyOwnerRepository.save(owner);
        return ResponseEntity.ok("Owner premium approved");
    }

    @PostMapping("/approvePremium")
    public ResponseEntity<String> approvePremium(@RequestParam String type,
                                                 @RequestParam Long id) {

        if (type.equalsIgnoreCase("USER")) {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            user.setPremiumActive(true);
            user.setPremiumStatus("APPROVED");
            userRepository.save(user);
            return ResponseEntity.ok("User premium approved");
        }
        else if (type.equalsIgnoreCase("OWNER")) {
            PropertyOwner owner = propertyOwnerRepository.findById(id).orElse(null);
            if (owner == null) {
                return ResponseEntity.badRequest().body("Owner not found");
            }
            owner.setPremiumActive(true);
            owner.setPremiumStatus("APPROVED");
            owner.setPremiumCount(owner.getPremiumCount() + 1);
            propertyOwnerRepository.save(owner);
            return ResponseEntity.ok("Owner premium approved");
        }
        return ResponseEntity.badRequest().body("Invalid type");
    }
}