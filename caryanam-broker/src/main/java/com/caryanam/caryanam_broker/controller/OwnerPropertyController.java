package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.Enum.PgType;
import com.caryanam.caryanam_broker.Enum.PropertyType;
import com.caryanam.caryanam_broker.appconstant.AppConstants;
import com.caryanam.caryanam_broker.configuration.CustomUserDetails;
import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.ResponseDto;
import com.caryanam.caryanam_broker.dto.ResponseHandler;
import com.caryanam.caryanam_broker.entity.Property;
import com.caryanam.caryanam_broker.entity.PropertyOwner;
import com.caryanam.caryanam_broker.messageconfig.MessageConfig;
import com.caryanam.caryanam_broker.repository.*;
import com.caryanam.caryanam_broker.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
public class OwnerPropertyController {

    @Autowired private PropertyService propertyService;
    @Autowired private PropertyImageRepository propertyImageRepository;
    @Autowired private PropertyOwnerRepository propertyOwnerRepository;
    @Autowired private PropertyRepository propertyRepository;

    // ================= COMMON METHODS =================

    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private Long getLoggedInOwnerId() {
        Authentication auth = getAuth();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        } else if (principal instanceof String) {
            String email = (String) principal;
            PropertyOwner owner = propertyOwnerRepository.findByEmail(email).orElse(null);
            return owner != null ? owner.getOwnerId() : null;
        }
        return null;
    }

    private boolean isAdmin() {
        Authentication auth = getAuth();
        if (auth == null) return false;

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ================= ADD PROPERTY =================
    @PostMapping("/addPropertyByOwner/{ownerId}")
    public ResponseEntity<Object> addProperty(@PathVariable Long ownerId,
                                              @RequestBody PropertyDto propertyDto) {

        Long loggedInOwnerId = getLoggedInOwnerId();

        if (loggedInOwnerId == null) {
            return ResponseEntity.status(401)
                    .body(new ResponseDto<>(401, MessageConfig.UNAUTHORIZED, null));
        }

        if (!isAdmin() && !loggedInOwnerId.equals(ownerId)) {
            return ResponseEntity.status(403)
                    .body(new ResponseDto<>(403, MessageConfig.FORBIDDEN, null));
        }

        // ===== YOUR VALIDATION (UNCHANGED) =====
        if (propertyDto.getTitle() == null || propertyDto.getTitle().trim().isEmpty())
            return ResponseHandler.generateResponse(MessageConfig.TITLE_REQUIRED, HttpStatus.BAD_REQUEST, null);

        for (int i = 0; i < propertyDto.getTitle().length(); i++)
            if (Character.isDigit(propertyDto.getTitle().charAt(i)))
                return ResponseHandler.generateResponse(MessageConfig.TITLE_INVALID, HttpStatus.BAD_REQUEST, null);

        if (propertyDto.getPrice() == null)
            return ResponseHandler.generateResponse(MessageConfig.PRICE_REQUIRED, HttpStatus.BAD_REQUEST, null);

        if (propertyDto.getPrice() <= 0)
            return ResponseHandler.generateResponse(MessageConfig.NO_ALPHABETS_ALLOWED, HttpStatus.BAD_REQUEST, null);

        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);
        if (owner == null)
            return ResponseHandler.generateResponse(MessageConfig.OWNER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);

        return ResponseHandler.generateResponse(
                MessageConfig.PROPERTY_ADDED,
                HttpStatus.OK,
                propertyService.addProperty(propertyDto, ownerId)
        );
    }

    // ================= GET PROPERTY =================
    @GetMapping("/getPropertyById/{id}")
    public ResponseEntity<Object> getPropertyById(@PathVariable Long id) {

        Long loggedInOwnerId = getLoggedInOwnerId();

        if (loggedInOwnerId == null)
            return ResponseHandler.generateResponse(MessageConfig.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, null);

        Property property = propertyRepository.findById(id).orElse(null);
        if (property == null)
            return ResponseHandler.generateResponse(MessageConfig.PROPERTY_NOT_FOUND, HttpStatus.BAD_REQUEST, null);

        // 🔥 ADMIN BYPASS ADDED HERE
        if (!isAdmin()) {
            if (property.getPropertyOwner() == null ||
                    !property.getPropertyOwner().getOwnerId().equals(loggedInOwnerId)) {

                return ResponseHandler.generateResponse(
                        MessageConfig.FORBIDDEN,
                        HttpStatus.FORBIDDEN,
                        null
                );
            }
        }

        return ResponseHandler.generateResponse(
                MessageConfig.PROPERTY_FETCHED,
                HttpStatus.OK,
                propertyService.getPropertyById(id)
        );
    }

    // ================= UPDATE =================
    @PutMapping("/updatePropertyById/{id}")
    public ResponseEntity<Object> updateProperty(@PathVariable Long id,
                                                 @RequestBody PropertyDto propertyDto) {

        Long loggedInOwnerId = getLoggedInOwnerId();

        if (loggedInOwnerId == null)
            return ResponseHandler.generateResponse(MessageConfig.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, null);

        Property property = propertyRepository.findById(id).orElse(null);
        if (property == null)
            return ResponseHandler.generateResponse(MessageConfig.PROPERTY_NOT_FOUND, HttpStatus.BAD_REQUEST, null);

        if (!isAdmin()) {
            if (property.getPropertyOwner() == null ||
                    !property.getPropertyOwner().getOwnerId().equals(loggedInOwnerId)) {

                return ResponseHandler.generateResponse(MessageConfig.FORBIDDEN, HttpStatus.FORBIDDEN, null);
            }
        }

        return ResponseHandler.generateResponse(
                MessageConfig.PROPERTY_UPDATED,
                HttpStatus.OK,
                propertyService.updateProperty(id, propertyDto)
        );
    }

    // ================= DELETE =================
    @DeleteMapping("/deletePropertyById/{id}")
    public ResponseEntity<Object> deleteProperty(@PathVariable Long id) {

        Long loggedInOwnerId = getLoggedInOwnerId();

        if (loggedInOwnerId == null)
            return ResponseHandler.generateResponse(MessageConfig.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, null);

        Property property = propertyRepository.findById(id).orElse(null);
        if (property == null)
            return ResponseHandler.generateResponse(MessageConfig.PROPERTY_NOT_FOUND, HttpStatus.BAD_REQUEST, null);

        if (!isAdmin()) {
            if (property.getPropertyOwner() == null ||
                    !property.getPropertyOwner().getOwnerId().equals(loggedInOwnerId)) {

                return ResponseHandler.generateResponse(MessageConfig.FORBIDDEN, HttpStatus.FORBIDDEN, null);
            }
        }

        return ResponseHandler.generateResponse(
                MessageConfig.PROPERTY_DELETED,
                HttpStatus.OK,
                propertyService.deleteProperty(id)
        );
    }

    // ================= IMAGE UPLOAD =================
    @PostMapping("/uploadPropertyImagesByPropertyId/{id}")
    public ResponseEntity<Object> uploadPropertyImages(@PathVariable Long id,
                                                       @RequestParam("files") MultipartFile[] files) {

        Long loggedInOwnerId = getLoggedInOwnerId();

        if (loggedInOwnerId == null)
            return ResponseHandler.generateResponse(MessageConfig.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, null);

        Property property = propertyRepository.findById(id).orElse(null);
        if (property == null)
            return ResponseHandler.generateResponse(MessageConfig.PROPERTY_NOT_FOUND, HttpStatus.BAD_REQUEST, null);

        if (!isAdmin()) {
            if (property.getPropertyOwner() == null ||
                    !property.getPropertyOwner().getOwnerId().equals(loggedInOwnerId)) {

                return ResponseHandler.generateResponse(MessageConfig.FORBIDDEN, HttpStatus.FORBIDDEN, null);
            }
        }

        return ResponseHandler.generateResponse(
                propertyService.uploadPropertyImages(id, files),
                HttpStatus.OK,
                null
        );
    }

    // ================= PREMIUM =================
    @PostMapping("/buyPremiumByOwner/{ownerId}")
    public ResponseEntity<Object> buyPremium(@PathVariable Long ownerId) {

        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);

        if (owner == null)
            return ResponseHandler.generateResponse(MessageConfig.OWNER_NOT_FOUND, HttpStatus.BAD_REQUEST, null);

        if ("PENDING".equalsIgnoreCase(owner.getPremiumStatus()))
            return ResponseHandler.generateResponse(MessageConfig.PAYMENT_ALREADY_IN_PROCESS, HttpStatus.BAD_REQUEST, null);

        owner.setPremiumStatus("PENDING");
        owner.setPremiumActive(false);
        owner.setPremiumCount(owner.getPremiumCount() + 1);

        propertyOwnerRepository.save(owner);

        Map<String, Object> res = new HashMap<>();
        res.put("message", MessageConfig.SCAN_QR);
        res.put("qrCode", "http://localhost:8080/qr/payment.png");
        res.put("status", "PENDING");

        return ResponseHandler.generateResponse(MessageConfig.PAYMENT_INITIATED, HttpStatus.OK, res);
    }
}