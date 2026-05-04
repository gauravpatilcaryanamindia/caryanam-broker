package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.Enum.PgType;
import com.caryanam.caryanam_broker.Enum.PropertyType;
import com.caryanam.caryanam_broker.appconstant.AppConstants;
import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.ResponseHandler;
import com.caryanam.caryanam_broker.entity.PropertyOwner;
import com.caryanam.caryanam_broker.messageconfig.MessageConfig;
import com.caryanam.caryanam_broker.repository.PropertyImageRepository;
import com.caryanam.caryanam_broker.repository.PropertyOwnerRepository;
import com.caryanam.caryanam_broker.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
public class OwnerPropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    @Autowired
    private PropertyOwnerRepository propertyOwnerRepository;


    @PostMapping("/addPropertyByOwner/{ownerId}")
    public ResponseEntity<Object> addProperty(
            @PathVariable Long ownerId,
            @RequestBody PropertyDto propertyDto) {
        if (propertyDto.getTitle() == null || propertyDto.getTitle().trim().isEmpty()) {
            return ResponseHandler.generateResponse(MessageConfig.TITLE_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        for (int i = 0; i < propertyDto.getTitle().length(); i++) {
            if (Character.isDigit(propertyDto.getTitle().charAt(i))) {
                return ResponseHandler.generateResponse(MessageConfig.TITLE_INVALID, HttpStatus.BAD_REQUEST, null);
            }
        }
        if (propertyDto.getPrice() == null) {
            return ResponseHandler.generateResponse(MessageConfig.PRICE_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        String priceStr = String.valueOf(propertyDto.getPrice());
        if (!priceStr.matches("\\d+(\\.\\d+)?")) {
            return ResponseHandler.generateResponse(MessageConfig.NO_ALPHABETS_ALLOWED, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getPrice() <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.NO_ALPHABETS_ALLOWED, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getLocation() == null || propertyDto.getLocation().trim().isEmpty()) {
            return ResponseHandler.generateResponse(MessageConfig.LOCATION_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        for (int i = 0; i < propertyDto.getLocation().length(); i++) {
            if (Character.isDigit(propertyDto.getLocation().charAt(i))) {
                return ResponseHandler.generateResponse(MessageConfig.LOCATION_INVALID, HttpStatus.BAD_REQUEST, null);
            }
        }
        if (propertyDto.getCity() == null || propertyDto.getCity().trim().isEmpty()) {
            return ResponseHandler.generateResponse(MessageConfig.CITY_IS_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getAddress() == null || propertyDto.getAddress().trim().isEmpty()) {
            return ResponseHandler.generateResponse(MessageConfig.ADDRESS_IS_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getState() == null || propertyDto.getState().trim().isEmpty()) {
            return ResponseHandler.generateResponse(MessageConfig.STATE_IS_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        String pincode = propertyDto.getPincode();
        if (pincode == null || pincode.trim().isEmpty() || !pincode.matches("[1-9][0-9]{5}")) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_PINCODE, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getDescription() == null || propertyDto.getDescription().trim().isEmpty()) {
            return ResponseHandler.generateResponse(MessageConfig.DESCRIPTION_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getPropertyType() == null && propertyDto.getPgType() == null) {
            return ResponseHandler.generateResponse("Either PropertyType or PgType is required", HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getPropertyType() != null && propertyDto.getPropertyType() == PropertyType.ALL) {
            return ResponseHandler.generateResponse(MessageConfig.PROPERTY_TYPE_INVALID, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getPgType() != null && propertyDto.getPgType() == PgType.ALL) {
            return ResponseHandler.generateResponse(MessageConfig.PG_TYPE_INVALID, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getBhkType() == null) {
            return ResponseHandler.generateResponse(MessageConfig.BHK_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getFurnishing() == null) {
            return ResponseHandler.generateResponse(MessageConfig.FURNISHING_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getMobileNumber() == null || propertyDto.getMobileNumber().length() != 10) {
            return ResponseHandler.generateResponse(MessageConfig.MOBILE_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        char firstDigit = propertyDto.getMobileNumber().charAt(0);
        if (firstDigit < '6' || firstDigit > '9') {
            return ResponseHandler.generateResponse(MessageConfig.MOBILE_INVALID_START, HttpStatus.BAD_REQUEST, null);
        }
        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            return ResponseHandler.generateResponse(MessageConfig.OWNER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        PropertyDto response = propertyService.addProperty(propertyDto, ownerId);
        if (response == null) {
            return ResponseHandler.generateResponse(MessageConfig.PROPERTY_NOT_ADDED, HttpStatus.BAD_REQUEST, null);
        }
        if (!owner.isPremiumActive()) {
            return ResponseHandler.generateResponse("Property added successfully. Buy Premium to publish it.", HttpStatus.OK, response);
        }
        return ResponseHandler.generateResponse(MessageConfig.PROPERTY_ADDED, HttpStatus.OK, response);
    }


    @GetMapping("/getPropertyById/{id}")
    public ResponseEntity<Object> getPropertyById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_ID, HttpStatus.BAD_REQUEST, null);
        }
        return ResponseHandler.generateResponse(MessageConfig.PROPERTY_FETCHED, HttpStatus.OK, propertyService.getPropertyById(id));
    }


    @PutMapping("/updatePropertyById/{id}")
    public ResponseEntity<Object> updateProperty(@PathVariable Long id, @RequestBody PropertyDto propertyDto) {
        if (id == null || id <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_ID, HttpStatus.BAD_REQUEST, null);
        }
        return ResponseHandler.generateResponse(MessageConfig.PROPERTY_UPDATED, HttpStatus.OK, propertyService.updateProperty(id, propertyDto));
    }


    @DeleteMapping("deletePropertyById/{id}")
    public ResponseEntity<Object> deleteProperty(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_ID, HttpStatus.BAD_REQUEST, null);
        }
        return ResponseHandler.generateResponse(propertyService.deleteProperty(id), HttpStatus.OK, MessageConfig.PROPERTY_DELETED);
    }

    @PostMapping("/uploadPropertyImagesByPropertyId/{id}")
    public ResponseEntity<Object> uploadPropertyImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseHandler.generateResponse(MessageConfig.IMAGE_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if (files.length > 10) {
            return ResponseHandler.generateResponse(MessageConfig.IMAGE_MAX_LIMIT, HttpStatus.BAD_REQUEST, null);
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                return ResponseHandler.generateResponse(MessageConfig.IMAGE_EMPTY, HttpStatus.BAD_REQUEST, null);
            }
            if (file.getSize() > 20 * 1024 * 1024) {
                return ResponseHandler.generateResponse(MessageConfig.IMAGE_SIZE_EXCEEDED, HttpStatus.BAD_REQUEST, null);
            }
            String fileName = file.getOriginalFilename();
            if (!(fileName.endsWith(AppConstants.JPG) || fileName.endsWith(AppConstants.JPEG) || fileName.endsWith(AppConstants.PNG))) {
                return ResponseHandler.generateResponse(MessageConfig.IMAGE_INVALID_FORMAT, HttpStatus.BAD_REQUEST, null);
            }
            if (propertyImageRepository.findByImageName(fileName) != null) {
                return ResponseHandler.generateResponse(MessageConfig.IMAGE_DUPLICATE, HttpStatus.BAD_REQUEST, null);
            }
        }
        String response = propertyService.uploadPropertyImages(id, files);
        if (response.equals(MessageConfig.PROPERTY_IMAGE_INCOMPLETE)) {
            return ResponseHandler.generateResponse(response, HttpStatus.OK, null);
        }
        return ResponseHandler.generateResponse(response, HttpStatus.OK, null);
    }

    @PostMapping("/buyPremiumByOwner/{ownerId}")
    public ResponseEntity<Object> buyPremium(@PathVariable Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_ID, HttpStatus.BAD_REQUEST, null);
        }
        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            return ResponseHandler.generateResponse(MessageConfig.OWNER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        if ("APPROVED".equalsIgnoreCase(owner.getPremiumStatus())) {
            owner.setPremiumStatus("NONE");
            owner.setPremiumActive(false);
        }
        if ("PENDING".equalsIgnoreCase(owner.getPremiumStatus())) {
            return ResponseHandler.generateResponse(MessageConfig.PAYMENT_ALREADY_IN_PROCESS, HttpStatus.BAD_REQUEST, null);
        }
        owner.setPremiumStatus("PENDING");
        owner.setPremiumActive(false);
        owner.setPremiumCount(owner.getPremiumCount() + 1);
        propertyOwnerRepository.save(owner);
        String qrUrl = "http://localhost:8080/qr/payment.png";
        Map<String, Object> response = new HashMap<>();
        response.put("message", MessageConfig.SCAN_QR);
        response.put("qrCode", qrUrl);
        response.put("status", "PENDING");
        return ResponseHandler.generateResponse(MessageConfig.PAYMENT_INITIATED, HttpStatus.OK, response);
    }
}