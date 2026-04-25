package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.dto.ResponseHandler;
import com.caryanam.caryanam_broker.entity.Property;
import com.caryanam.caryanam_broker.entity.PropertyOwner;
import com.caryanam.caryanam_broker.entity.User;
import com.caryanam.caryanam_broker.messageconfig.MessageConfig;
import com.caryanam.caryanam_broker.repository.PropertyOwnerRepository;
import com.caryanam.caryanam_broker.repository.PropertyRepository;
import com.caryanam.caryanam_broker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class adminController {

    @Autowired
    private PropertyOwnerRepository propertyOwnerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;


    @GetMapping("/pending-users")
    public List<User> getPendingUsers() {
        return userRepository.findByPremiumStatus("PENDING");
    }

    @GetMapping("/pending-Owner")
    public List<PropertyOwner> getPendingAdmins() {
        return propertyOwnerRepository.findByPremiumStatus("PENDING");
    }
    @PostMapping("/approveOwnerPremium/{ownerId}")
    public ResponseEntity<Object> approveOwner(@PathVariable Long ownerId) {
        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            return ResponseHandler.generateResponse(MessageConfig.OWNER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        owner.setPremiumStatus("APPROVED");
        owner.setPremiumActive(true);
        propertyOwnerRepository.save(owner);
        return ResponseHandler.generateResponse(MessageConfig.OWNER_PREMIUM_APPROVED, HttpStatus.OK, null);
    }

    @PostMapping("/approveUserPremium/{userId}")
    public ResponseEntity<Object> approveUser(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_ID, HttpStatus.BAD_REQUEST, null);
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseHandler.generateResponse(MessageConfig.USER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        user.setPremiumStatus("APPROVED");
        user.setPremiumActive(true);
        userRepository.save(user);
        return ResponseHandler.generateResponse(MessageConfig.USER_PREMIUM_APPROVED, HttpStatus.OK, user);
    }

    @PostMapping("/rejectOwnerPremium/{ownerId}")
    public ResponseEntity<Object> rejectOwner(@PathVariable Long ownerId) {
        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            return ResponseHandler.generateResponse(MessageConfig.OWNER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        owner.setPremiumStatus("REJECTED");
        owner.setPremiumActive(false);
        propertyOwnerRepository.save(owner);
        return ResponseHandler.generateResponse(MessageConfig.OWNER_PREMIUM_REJECTED, HttpStatus.OK, null);
    }

    @PostMapping("/rejectUserPremium/{userId}")
    public ResponseEntity<Object> rejectUser(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_ID, HttpStatus.BAD_REQUEST, null);
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseHandler.generateResponse(MessageConfig.USER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        user.setPremiumStatus("REJECTED");
        user.setPremiumActive(false);
        userRepository.save(user);
        return ResponseHandler.generateResponse(MessageConfig.USER_PREMIUM_REJECTED, HttpStatus.OK, null);
    }

    @GetMapping("/owner/{ownerId}/properties")
    public ResponseEntity<?> getOwnerProperties(@PathVariable Long ownerId) {
        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            return ResponseHandler.generateResponse(MessageConfig.OWNER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        List<Property> properties = propertyRepository.findByPropertyOwner_OwnerId(ownerId);
        Map<String, Object> response = new HashMap<>();
        response.put("ownerId", ownerId);
        response.put("ownerName", owner.getFullName());
        response.put("totalProperties", properties.size());
        response.put("properties", properties);
        return ResponseEntity.ok(response);
    }
}