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
    public ResponseEntity<String> buyPremium(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        if ("APPROVED".equalsIgnoreCase(user.getPremiumStatus())) {
            user.setPremiumStatus("NONE");
            user.setPremiumActive(false);
        }
        if ("PENDING".equalsIgnoreCase(user.getPremiumStatus())) {
            return ResponseEntity.badRequest().body("Payment already in process");
        }
        user.setPremiumStatus("PENDING");
        user.setPremiumActive(false);
        user.setPremiumCount(user.getPremiumCount() + 1);
        userRepository.save(user);

        return ResponseEntity.ok("User premium request sent");
    }

    @GetMapping("/properties/{userId}")
    public ResponseEntity<?> getProperties(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        if (!user.isPremiumActive()) {
            return ResponseEntity.badRequest().body("Premium not active");
        }
        return ResponseEntity.ok(propertyService.getAllProperties(userId));
    }

    @PostMapping("/filter-properties/{userId}")
    public ResponseEntity<Object> filterProperties(@RequestBody PropertyFilterDto dto, @PathVariable Long userId) {
        List<PropertyDto> data = propertyService.filterProperties(dto, userId);
        return ResponseHandler.generateResponse(MessageConfig.PROPERTY_FILTERED, HttpStatus.OK, data);
    }


}