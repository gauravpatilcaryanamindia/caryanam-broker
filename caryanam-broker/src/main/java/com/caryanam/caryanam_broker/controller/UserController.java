package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.PropertyFilterDto;
import com.caryanam.caryanam_broker.dto.ResponseHandler;
import com.caryanam.caryanam_broker.entity.User;
import com.caryanam.caryanam_broker.messageconfig.MessageConfig;
import com.caryanam.caryanam_broker.repository.UserRepository;
import com.caryanam.caryanam_broker.service.PropertyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyService propertyService;

    @PostMapping("/buyPremium/{userId}")
    public ResponseEntity<Object> buyPremium(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_ID, HttpStatus.BAD_REQUEST, null);
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseHandler.generateResponse(
                    MessageConfig.USER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        if ("APPROVED".equalsIgnoreCase(user.getPremiumStatus())) {
            user.setPremiumStatus("NONE");
            user.setPremiumActive(false);
        }
        if ("PENDING".equalsIgnoreCase(user.getPremiumStatus())) {
            return ResponseHandler.generateResponse(MessageConfig.PAYMENT_ALREADY_IN_PROCESS, HttpStatus.BAD_REQUEST, null);
        }
        user.setPremiumStatus("PENDING");
        user.setPremiumActive(false);
        user.setPremiumCount(user.getPremiumCount() + 1);
        userRepository.save(user);
        String qrUrl = "http://localhost:8080/qr/payment.png";
        Map<String, Object> response = new HashMap<>();
        response.put("message", MessageConfig.SCAN_QR);
        response.put("qrCode", qrUrl);
        response.put("status", "PENDING");
        return ResponseHandler.generateResponse(
                MessageConfig.PAYMENT_INITIATED,
                HttpStatus.OK,
                response
        );
    }
    @GetMapping("/properties/{userId}")
    public ResponseEntity<Object> getProperties(
            @PathVariable Long userId,
            HttpServletRequest request) {

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseHandler.generateResponse(
                    MessageConfig.USER_NOT_FOUND,
                    HttpStatus.BAD_REQUEST,
                    null
            );
        }

        List<PropertyDto> data = propertyService.getAllProperties(userId, request);

        if (data.isEmpty()) {
            return ResponseHandler.generateResponse(
                    MessageConfig.NO_PROPERTIES_FOUND,
                    HttpStatus.OK,
                    data
            );
        }
        if (!user.isPremiumActive()) {
            return ResponseHandler.generateResponse(
                    MessageConfig.PREMIUM_REQUIRED,
                    HttpStatus.OK,
                    data
            );
        }

        return ResponseHandler.generateResponse(
                MessageConfig.PROPERTY_FETCHED,
                HttpStatus.OK,
                data
        );
    }

    @PostMapping("/filter-properties/{userId}")
    public ResponseEntity<Object> filterProperties(@RequestBody PropertyFilterDto dto, @PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseHandler.generateResponse(
                    MessageConfig.USER_NOT_FOUND,
                    HttpStatus.BAD_REQUEST,
                    null
            );
        }
        List<PropertyDto> data = propertyService.filterProperties(dto, userId);
        if (!user.isPremiumActive()) {
            return ResponseHandler.generateResponse(
                    MessageConfig.PREMIUM_REQUIRED,
                    HttpStatus.OK,
                    data
            );
        }
        if (data.isEmpty()) {
            return ResponseHandler.generateResponse(
                    MessageConfig.NO_PROPERTIES_FOUND,
                    HttpStatus.OK,
                    data
            );
        }
        return ResponseHandler.generateResponse(
                MessageConfig.PROPERTY_FILTERED,
                HttpStatus.OK,
                data
        );
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

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(
            @PathVariable Long userId
    ) {

        if (userId == null || userId <= 0) {
            return ResponseHandler.generateResponse(
                    "Invalid User Id",
                    HttpStatus.BAD_REQUEST,
                    null
            );
        }

        User user = userRepository
                .findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseHandler.generateResponse(
                    "User not found",
                    HttpStatus.NOT_FOUND,
                    null
            );
        }

        Map<String, Object> response = new HashMap<>();

        response.put("id", user.getUserId());
        response.put("premiumStatus", user.getPremiumStatus());
        response.put("premiumActive", user.isPremiumActive());

        return ResponseHandler.generateResponse(
                "User fetched successfully",
                HttpStatus.OK,
                response
        );
    }

}