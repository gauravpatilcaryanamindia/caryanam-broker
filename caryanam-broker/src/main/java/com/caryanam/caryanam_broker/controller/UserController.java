package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.PropertyFilterDto;
import com.caryanam.caryanam_broker.dto.ResponseHandler;
import com.caryanam.caryanam_broker.entity.User;
import com.caryanam.caryanam_broker.messageconfig.MessageConfig;
import com.caryanam.caryanam_broker.repository.UserRepository;
import com.caryanam.caryanam_broker.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyService propertyService;

    @PostMapping("/buyPremium/{userId}")
    public ResponseEntity<Object> buyPremium(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) {
            return ResponseHandler.generateResponse(MessageConfig.USER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        if ("APPROVED".equalsIgnoreCase(user.getPremiumStatus())) {user.setPremiumStatus("NONE");user.setPremiumActive(false);
        }
        if ("PENDING".equalsIgnoreCase(user.getPremiumStatus())) {
            return ResponseHandler.generateResponse(MessageConfig.PAYMENT_ALREADY_IN_PROCESS, HttpStatus.BAD_REQUEST, null);
        }
        user.setPremiumStatus("PENDING");
        user.setPremiumActive(false);
        user.setPremiumCount(user.getPremiumCount() + 1);
        userRepository.save(user);
        return ResponseHandler.generateResponse(MessageConfig.PREMIUM_REQUEST_SENT, HttpStatus.OK, null);
    }

    @GetMapping("/properties/{userId}")
    public ResponseEntity<?> getProperties(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseHandler.generateResponse(MessageConfig.USER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        if (!user.isPremiumActive()) {
            return ResponseHandler.generateResponse(MessageConfig.PREMIUM_NOT_ACTIVE, HttpStatus.BAD_REQUEST, null);
        }
        return ResponseEntity.ok(propertyService.getAllProperties(userId));
    }

    @PostMapping("/filter-properties/{userId}")
    public ResponseEntity<Object> filterProperties(@RequestBody PropertyFilterDto dto, @PathVariable Long userId) {
        List<PropertyDto> data = propertyService.filterProperties(dto, userId);
        return ResponseHandler.generateResponse(MessageConfig.PROPERTY_FILTERED, HttpStatus.OK, data);
    }

    @GetMapping("/properties-by-city")
    public ResponseEntity<?> getProperties(@RequestParam String city, @RequestParam(required = false) String address) {
        if (city == null || city.trim().isEmpty()) {
            return ResponseHandler.generateResponse(MessageConfig.CITY_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if (address == null) {
            List<String> addresses = propertyService.getAddressesByCity(city);
            return ResponseEntity.ok(addresses);
        }return ResponseEntity.ok(propertyService.getPropertiesByCityAndAddress(city, address));
    }
}