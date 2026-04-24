package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.Enum.PgType;
import com.caryanam.caryanam_broker.Enum.PropertyType;
import com.caryanam.caryanam_broker.appconstant.AppConstants;
import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.PropertyFilterDto;
import com.caryanam.caryanam_broker.dto.ResponseHandler;
import com.caryanam.caryanam_broker.entity.Admin;
import com.caryanam.caryanam_broker.entity.PropertyOwner;
import com.caryanam.caryanam_broker.messageconfig.MessageConfig;
import com.caryanam.caryanam_broker.repository.AdminRepository;
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
@RequestMapping("/api/admin")
public class AdminPropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    @Autowired
    private PropertyOwnerRepository propertyOwnerRepository;

    @PostMapping("/addPropertyByAdmin/{adminId}")
    public ResponseEntity<Object> addProperty(
            @PathVariable Long adminId, @RequestBody PropertyDto propertyDto) {
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
        if (propertyDto.getPrice() <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.PRICE_REQUIRED, HttpStatus.BAD_REQUEST, null);
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
            return ResponseHandler.generateResponse(MessageConfig.STATE_IS_REQUIRED , HttpStatus.BAD_REQUEST, null);
        }

        String pincode = propertyDto.getPincode();


        if (pincode == null || pincode.trim().isEmpty()) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_PINCODE, HttpStatus.BAD_REQUEST, null);
        }

        pincode = pincode.trim();

        if (!pincode.matches("[1-9][0-9]{5}")) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_PINCODE, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getDescription() == null || propertyDto.getDescription().trim().isEmpty()) {
            return ResponseHandler.generateResponse(MessageConfig.DESCRIPTION_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getPropertyType() == null) {
            return ResponseHandler.generateResponse(MessageConfig.PROPERTY_TYPE_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if (propertyDto.getPropertyType() == PropertyType.ALL) {
            return ResponseHandler.generateResponse(MessageConfig.PROPERTY_TYPE_INVALID, HttpStatus.BAD_REQUEST, null);
        }
        if(propertyDto.getPgType()== null){
            return ResponseHandler.generateResponse(MessageConfig.PG_TYPE_REQUIRED, HttpStatus.BAD_REQUEST, null);
        }
        if(propertyDto.getPgType()== PgType.ALL){
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
        PropertyDto response = propertyService.addProperty(propertyDto, adminId);
        if (response == null) {
            return ResponseHandler.generateResponse(MessageConfig.PROPERTY_LIMIT_EXCEEDED, HttpStatus.BAD_REQUEST, null);
        }
        return ResponseHandler.generateResponse(MessageConfig.PROPERTY_ADDED, HttpStatus.OK, response);
    }


    @GetMapping("/get-all-properties/{userId}")
    public ResponseEntity<Object> getAllProperties(@PathVariable Long Id) {
        return ResponseHandler.generateResponse(
                MessageConfig.PROPERTY_FETCHED,
                HttpStatus.OK,
                propertyService.getAllProperties(Id)
        );
    }

    // 3. Get Property By Id
    @GetMapping("/getPropertyById/{id}")
    public ResponseEntity<Object> getPropertyById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_ID, HttpStatus.BAD_REQUEST, null);
        }
        return ResponseHandler.generateResponse(MessageConfig.PROPERTY_FETCHED, HttpStatus.OK, propertyService.getPropertyById(id));
    }

    // 4. Update Property
    @PutMapping("/updatePropertyById/{id}")
    public ResponseEntity<Object> updateProperty(@PathVariable Long id, @RequestBody PropertyDto propertyDto) {
        if (id == null || id <= 0) {
            return ResponseHandler.generateResponse(MessageConfig.INVALID_ID, HttpStatus.BAD_REQUEST, null);
        }
        return ResponseHandler.generateResponse(MessageConfig.PROPERTY_UPDATED, HttpStatus.OK, propertyService.updateProperty(id, propertyDto));
    }

    // 5. Delete Property
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


    @PostMapping("/buyPremiumByAdminId/{ownerId}")
    public ResponseEntity<Object> buyPremium(@PathVariable Long ownerId) {

        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);

        if (owner == null) {
            return ResponseHandler.generateResponse("Owner not found", HttpStatus.BAD_REQUEST, null);
        }

        if (owner.isPremiumActive()) {
            return ResponseHandler.generateResponse("Premium already active", HttpStatus.BAD_REQUEST, null);
        }

        owner.setPremiumStatus("PENDING");
        owner.setPremiumActive(false);

        propertyOwnerRepository.save(owner);

        String qrUrl = "http://localhost:8080/qr/payment.png";

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Scan QR & complete payment");
        response.put("qrCode", qrUrl);
        response.put("status", "PENDING");

        return ResponseHandler.generateResponse("Payment initiated", HttpStatus.OK, response);
    }

}