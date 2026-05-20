package com.caryanam.caryanam_broker.serviceimpl;

import com.caryanam.caryanam_broker.appconstant.AppConstants;
import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.PropertyFilterDto;
import com.caryanam.caryanam_broker.entity.*;
import com.caryanam.caryanam_broker.messageconfig.MessageConfig;
import com.caryanam.caryanam_broker.repository.*;
import com.caryanam.caryanam_broker.service.AreaPincodeService;
import com.caryanam.caryanam_broker.service.PropertyService;
import jakarta.servlet.http.HttpServletRequest;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Service
public class PropertyServiceImpl implements PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyImageRepository propertyImageRepository;


    @Autowired
    private PropertyOwnerRepository propertyOwnerRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AreaPincodeService areaPincodeService;

    private String toImageDataUrl(PropertyImage image) {
        if (image == null || image.getImageData() == null || image.getImageData().length == 0) {
            return null;
        }

        String contentType = image.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "image/jpeg";
        }

        return "data:" + contentType + ";base64," +
                Base64.getEncoder().encodeToString(image.getImageData());
    }

    private void attachDatabaseImages(PropertyDto dto, Long propertyId) {
        List<PropertyImage> images =
                propertyImageRepository.findByPropertyId(propertyId);

        List<String> doctypeImageBase64List = new ArrayList<>();
        List<String> doctypeImageNames = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                PropertyImage image = images.get(i);
                String dataUrl = toImageDataUrl(image);

                if (i == 0) {
                    dto.setCoverImage(image.getImageName());
                    dto.setCoverImageBase64(dataUrl);
                } else {
                    if (dataUrl != null) {
                        doctypeImageBase64List.add(dataUrl);
                    }
                    if (image.getImageName() != null) {
                        doctypeImageNames.add(image.getImageName());
                    }
                }
            }
        }

        dto.setDoctypeImageBase64List(doctypeImageBase64List);
        dto.setDoctypeImages(String.valueOf(doctypeImageNames));
    }

    private String getImageOutputFormat(String contentType, String originalName) {
        String normalizedContentType = String.valueOf(contentType).toLowerCase();
        String normalizedName = String.valueOf(originalName).toLowerCase();

        if (normalizedContentType.contains("png") || normalizedName.endsWith(".png")) {
            return "png";
        }

        return "jpg";
    }

    private String getResponseContentType(String outputFormat, String contentType) {
        if ("png".equals(outputFormat)) {
            return "image/png";
        }

        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }

        return "image/jpeg";
    }

    private boolean matchesText(String source, String filter) {
        return source != null
                && filter != null
                && source.trim().equalsIgnoreCase(filter.trim());
    }

    private boolean matchesLocationOrAddress(Property property, String filter) {
        return matchesText(property.getLocation(), filter)
                || matchesText(property.getAddress(), filter);
    }



    @Override
    public PropertyDto addProperty(PropertyDto propertyDto, Long ownerId) {

        PropertyOwner owner = propertyOwnerRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            return null;
        }
        Property property = new Property();
        property.setTitle(propertyDto.getTitle());
        property.setPrice(propertyDto.getPrice());
        property.setLocation(propertyDto.getLocation());
        property.setAddress(propertyDto.getAddress());
        property.setCity(propertyDto.getCity());
        property.setState(propertyDto.getState());
        property.setPincode(propertyDto.getPincode());
        property.setDescription(propertyDto.getDescription());
        property.setPropertyType(propertyDto.getPropertyType());
        property.setPgType(propertyDto.getPgType());
        property.setBhkType(propertyDto.getBhkType());
        property.setFurnishing(propertyDto.getFurnishing());
        property.setCarpetArea(propertyDto.getCarpetArea());
        property.setMobileNumber(propertyDto.getMobileNumber());
        property.setApartmentName(propertyDto.getApartmentName());


        property.setLikesCount(0);
        property.setViewsCount(0);
        if (owner.isPremiumActive()) {
            property.setStatus(AppConstants.ACTIVE);
        } else {
            property.setStatus(AppConstants.PENDING);
        }
        property.setPropertyOwner(owner);
        Property saved = propertyRepository.save(property);
        PropertyDto dto = new PropertyDto();
        dto.setId(saved.getId());
        dto.setTitle(saved.getTitle());
        dto.setPrice(saved.getPrice());
        dto.setStatus(saved.getStatus());

        return dto;
    }


    @Override
    public List<PropertyDto> getAllProperties(Long userId, HttpServletRequest request) {

        boolean isPremium = false;

        if (request.getAttribute("isPremium") != null) {
            isPremium = (boolean) request.getAttribute("isPremium");
        }

        // ONLY ACTIVE + APPROVED PAYMENT STATUS
        List<Property> properties =
                propertyRepository.findByStatus(AppConstants.ACTIVE);

        List<PropertyDto> dtoList = new ArrayList<>();

        for (Property property : properties) {

            // PAYMENT STATUS CHECK
            // PENDING PAYMENT PROPERTIES HIDE
            if (property.getPaymentStatus() == null
                    || !property.getPaymentStatus().equalsIgnoreCase("APPROVED")) {
                continue;
            }

            PropertyOwner owner = property.getPropertyOwner();

            // OWNER NULL CHECK
            if (owner == null) {
                continue;
            }

            // OWNER PREMIUM ACTIVE CHECK
            if (!owner.isPremiumActive()) {
                continue;
            }

            // OWNER PREMIUM APPROVED CHECK
            if (owner.getPremiumStatus() == null
                    || !owner.getPremiumStatus().contains("APPROVED")) {
                continue;
            }

            PropertyDto dto = new PropertyDto();
            attachDatabaseImages(dto, property.getId());

            // NON PREMIUM USER
            if (!isPremium) {

                dto.setId(property.getId());
                dto.setTitle(property.getTitle());
                dto.setPrice(property.getPrice());
                dto.setLocation(property.getLocation());
                dto.setBhkType(property.getBhkType());
                dto.setCity(property.getCity());
                dto.setAddress(property.getAddress());
                dto.setNearBy(property.getNearBy());
                dto.setPincode(property.getPincode());
                if (property.getPincode() != null && !property.getPincode().isBlank()) {
                    List<String> nearbyAreas =
                            areaPincodeService.getNearbyData(property.getPincode());

                    dto.setNearBy(String.valueOf(nearbyAreas));
                }
            } else {

                // PREMIUM USER → FULL DETAILS
                dto.setId(property.getId());
                dto.setTitle(property.getTitle());
                dto.setPrice(property.getPrice());
                dto.setLocation(property.getLocation());
                dto.setAddress(property.getAddress());
                dto.setCity(property.getCity());
                dto.setState(property.getState());
                dto.setPincode(property.getPincode());
                dto.setDescription(property.getDescription());
                dto.setPropertyType(property.getPropertyType());
                dto.setPgType(property.getPgType());
                dto.setBhkType(property.getBhkType());
                dto.setNearBy(property.getNearBy());
                dto.setFurnishing(property.getFurnishing());
                dto.setCarpetArea(property.getCarpetArea());
                dto.setMobileNumber(property.getMobileNumber());
                dto.setLikesCount(property.getLikesCount());
                dto.setViewsCount(property.getViewsCount());
                dto.setApartmentName(property.getApartmentName());
                dto.setStatus(property.getStatus());

                dto.setOwnerId(owner.getOwnerId());
                dto.setOwnerName(owner.getFullName());

            }

            dtoList.add(dto);
        }

        return dtoList;
    }


    @Override
    public PropertyDto getPropertyById(Long id) {
        Property property = propertyRepository.findById(id).orElse(null);

        if (property == null) {
            return null;
        }
        PropertyOwner owner = property.getPropertyOwner();
        if (owner == null) {
            return null;
        }
        PropertyDto dto = new PropertyDto();
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setPrice(property.getPrice());
        dto.setLocation(property.getLocation());
        dto.setAddress(property.getAddress());
        dto.setCity(property.getCity());
        dto.setState(property.getState());
        dto.setPincode(property.getPincode());
        dto.setDescription(property.getDescription());
        dto.setPropertyType(property.getPropertyType());
        dto.setPgType(property.getPgType());
        dto.setBhkType(property.getBhkType());
        dto.setFurnishing(property.getFurnishing());
        dto.setCarpetArea(property.getCarpetArea());
        dto.setMobileNumber(property.getMobileNumber());
        dto.setLikesCount(property.getLikesCount());
        dto.setViewsCount(property.getViewsCount());
        dto.setApartmentName(property.getApartmentName());
        dto.setStatus(property.getStatus());

        // PROPERTY PAYMENT STATUS
        dto.setPaymentStatus(property.getPaymentStatus());
        dto.setPremiumActive(property.isPremiumActive());

        dto.setOwnerId(owner.getOwnerId());

        attachDatabaseImages(dto, id);

        return dto;
    }

    @Override
    public PropertyDto updateProperty(Long id, PropertyDto propertyDto) {

        Property property = propertyRepository.findById(id).orElse(null);
        if (property == null) {
            return null;
        }
        if (propertyDto.getTitle() != null) {
            property.setTitle(propertyDto.getTitle());
        }
        if (propertyDto.getPrice() != null) {
            property.setPrice(propertyDto.getPrice());
        }
        if (propertyDto.getLocation() != null) {
            property.setLocation(propertyDto.getLocation());
        }
        if (propertyDto.getAddress() != null) {
            property.setAddress(propertyDto.getAddress());
        }
        if (propertyDto.getCity() != null) {
            property.setCity(propertyDto.getCity());
        }
        if (propertyDto.getState() != null) {
            property.setState(propertyDto.getState());
        }
        if (propertyDto.getPincode() != null) {
            property.setPincode(propertyDto.getPincode());
        }

        if (propertyDto.getDescription() != null) {
            property.setDescription(propertyDto.getDescription());
        }
        if (propertyDto.getPropertyType() != null) {
            property.setPropertyType(propertyDto.getPropertyType());
        }
        if (propertyDto.getPgType() != null) {
            property.setPgType(propertyDto.getPgType());
        }
        if (propertyDto.getBhkType() != null) {
            property.setBhkType(propertyDto.getBhkType());
        }
        if (propertyDto.getFurnishing() != null) {
            property.setFurnishing(propertyDto.getFurnishing());
        }
        if (propertyDto.getCarpetArea() != null) {
            property.setCarpetArea(propertyDto.getCarpetArea());
        }
        if (propertyDto.getMobileNumber() != null) {
            property.setMobileNumber(propertyDto.getMobileNumber());
        }
        if (propertyDto.getApartmentName() != null) {
            property.setApartmentName(propertyDto.getApartmentName());
        }
        Property updatedProperty = propertyRepository.save(property);
        PropertyDto responseDto = new PropertyDto();
        responseDto.setId(updatedProperty.getId());
        responseDto.setTitle(updatedProperty.getTitle());
        responseDto.setPrice(updatedProperty.getPrice());
        responseDto.setLocation(updatedProperty.getLocation());
        responseDto.setAddress(updatedProperty.getAddress());
        responseDto.setCity(updatedProperty.getCity());
        responseDto.setState(updatedProperty.getState());
        responseDto.setPincode(updatedProperty.getPincode());
        responseDto.setDescription(updatedProperty.getDescription());
        responseDto.setPropertyType(updatedProperty.getPropertyType());
        responseDto.setPgType(updatedProperty.getPgType());
        responseDto.setBhkType(updatedProperty.getBhkType());
        responseDto.setFurnishing(updatedProperty.getFurnishing());
        responseDto.setCarpetArea(updatedProperty.getCarpetArea());
        responseDto.setMobileNumber(updatedProperty.getMobileNumber());
        responseDto.setApartmentName(updatedProperty.getApartmentName());
        responseDto.setStatus(updatedProperty.getStatus());
        responseDto.setLikesCount(updatedProperty.getLikesCount());
        responseDto.setViewsCount(updatedProperty.getViewsCount());
        attachDatabaseImages(responseDto, updatedProperty.getId());

        return responseDto;
    }

    @Override
    public String deleteProperty(Long id) {
        Property property = propertyRepository.findById(id).orElse(null);
        if (property == null) {
            return MessageConfig.PROPERTY_NOT_FOUND;
        }
        property.setStatus(AppConstants.INACTIVE);
        propertyRepository.save(property);
        return AppConstants.PROPERTY_DELETED;
    }

    @Override
    public String uploadPropertyImages(Long propertyId, MultipartFile[] files) {
        Property property = propertyRepository.findById(propertyId).orElse(null);
        if (property == null) {
            return MessageConfig.PROPERTY_NOT_FOUND;
        }
        int index = 0;
        StringBuilder doctypeImages = new StringBuilder();
        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalName;
            Long originalKb = file.getSize() / 1024;
            Double originalMb = file.getSize() / (1024.0 * 1024.0);
            try {
                String contentType = file.getContentType();
                String outputFormat = getImageOutputFormat(contentType, originalName);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Thumbnails.of(file.getInputStream())
                        .scale(1.0)
                        .outputQuality(0.5)
                        .outputFormat(outputFormat)
                        .toOutputStream(outputStream);

                byte[] imageBytes = outputStream.toByteArray();
                Long compressedKb = (long) imageBytes.length / 1024;
                Double compressedMb = imageBytes.length / (1024.0 * 1024.0);
                PropertyImage image = new PropertyImage();
                image.setImageName(fileName);
                image.setImagePath(fileName);
                image.setContentType(getResponseContentType(outputFormat, contentType));
                image.setImageData(imageBytes);
                image.setOriginalSizeKb(originalKb);
                image.setOriginalSizeMb(originalMb);
                image.setCompressedSizeKb(compressedKb);
                image.setCompressedSizeMb(compressedMb);
                image.setProperty(property);
                propertyImageRepository.save(image);
                if (index == 0) {
                    property.setCoverImage(fileName);
                } else {
                    if (doctypeImages.length() > 0) {
                        doctypeImages.append(",");
                    }
                    doctypeImages.append(fileName);
                }
                index++;
            } catch (Exception e) {
                e.printStackTrace();
                return MessageConfig.IMAGE_UPLOAD_FAILED;
            }
        }
        if (doctypeImages.length() > 0) {
            property.setDoctypeImages(doctypeImages.toString());
        }
        int totalImages = propertyImageRepository.countByPropertyId(propertyId);
        property.setStatus(AppConstants.ACTIVE);
        propertyRepository.save(property);
        if (totalImages < 4) {
            return AppConstants.UPLOAD_SUCCESSFULLY + (4 - totalImages) + AppConstants.MORE_IMG;
        }
        return MessageConfig.IMAGE_UPLOAD_SUCCESS;
    }

    @Override
    public List<?> filterProperties(PropertyFilterDto filterDto, Long userId) {

        User user = userRepository.findById(userId).orElse(null);

        boolean isPremium = false;

        if (user != null && user.isPremiumActive()) {
            isPremium = true;
        }

        // ONLY ACTIVE PROPERTIES
        List<Property> allProperties =
                propertyRepository.findByStatus(AppConstants.ACTIVE);

        List<Property> filteredList = new ArrayList<>();

        for (Property property : allProperties) {

            // PAYMENT STATUS CHECK
            // ONLY APPROVED PAYMENT STATUS SHOW
            if (property.getPaymentStatus() == null
                    || !property.getPaymentStatus().equalsIgnoreCase("APPROVED")) {
                continue;
            }

            PropertyOwner owner = property.getPropertyOwner();

            if (owner == null) {
                continue;
            }

            // OWNER PREMIUM ACTIVE CHECK
            if (!owner.isPremiumActive()) {
                continue;
            }

            // OWNER PREMIUM APPROVED CHECK
            if (owner.getPremiumStatus() == null
                    || !owner.getPremiumStatus().contains("APPROVED")) {
                continue;
            }

            boolean match = true;

            // CITY FILTER
            if (filterDto.getCity() != null
                    && !filterDto.getCity().isEmpty()) {

                if (!property.getCity()
                        .equalsIgnoreCase(filterDto.getCity())) {
                    match = false;
                }
            }

            // ADDRESS FILTER
            if (filterDto.getAddress() != null
                    && !filterDto.getAddress().isBlank()) {

                if (!matchesLocationOrAddress(property, filterDto.getAddress())) {
                    match = false;
                }
            }

            // PROPERTY TYPE FILTER
            if (filterDto.getPropertyType() != null
                    && !filterDto.getPropertyType().isEmpty()
                    && !filterDto.getPropertyType().equalsIgnoreCase("ALL")) {

                if (!property.getPropertyType().name()
                        .equalsIgnoreCase(filterDto.getPropertyType())) {
                    match = false;
                }
            }

            // PG TYPE FILTER
            if (filterDto.getPgType() != null
                    && !filterDto.getPgType().isEmpty()
                    && !filterDto.getPgType().equalsIgnoreCase("ALL")) {

                if (property.getPgType() == null
                        || !property.getPgType().name()
                        .equalsIgnoreCase(filterDto.getPgType())) {

                    match = false;
                }
            }

            // MIN PRICE FILTER
            if (filterDto.getMinPrice() != null
                    && property.getPrice() < filterDto.getMinPrice()) {

                match = false;
            }

            // MAX PRICE FILTER
            if (filterDto.getMaxPrice() != null
                    && property.getPrice() > filterDto.getMaxPrice()) {

                match = false;
            }

            if (match) {
                filteredList.add(property);
            }
        }

        List<PropertyDto> dtoList = new ArrayList<>();

        for (Property property : filteredList) {

            PropertyDto dto = new PropertyDto();
            attachDatabaseImages(dto, property.getId());

            // NON PREMIUM USER
            if (!isPremium) {

                dto.setId(property.getId());
                dto.setTitle(property.getTitle());
                dto.setPrice(property.getPrice());
                dto.setLocation(property.getLocation());
                dto.setAddress(property.getAddress());
                dto.setCity(property.getCity());
                dto.setBhkType(property.getBhkType());
                dto.setPropertyType(property.getPropertyType());
                dto.setApartmentName(property.getApartmentName());

            } else {

                // PREMIUM USER FULL DETAILS
                dto.setId(property.getId());
                dto.setTitle(property.getTitle());
                dto.setPrice(property.getPrice());
                dto.setLocation(property.getLocation());
                dto.setAddress(property.getAddress());
                dto.setCity(property.getCity());
                dto.setBhkType(property.getBhkType());
                dto.setMobileNumber(property.getMobileNumber());
                dto.setDescription(property.getDescription());
                dto.setPropertyType(property.getPropertyType());
                dto.setApartmentName(property.getApartmentName());
            }

            // OWNER DETAILS
            if (property.getPropertyOwner() != null) {
                dto.setOwnerId(property.getPropertyOwner().getOwnerId());
            }

            dtoList.add(dto);
        }

        return dtoList;
    }
    public List<PropertyDto> getPropertiesByCityAndAddress(String city, String address) {
        List<Property> list;
        if (address == null || address.isEmpty()) {
            list = propertyRepository
                    .findByCityIgnoreCaseAndStatus(city, AppConstants.ACTIVE);

        } else {
            list = propertyRepository
                    .findByCityIgnoreCaseAndAddressIgnoreCaseAndStatus(city, address, AppConstants.ACTIVE);
        }
        List<PropertyDto> dtoList = new ArrayList<>();
        for (Property property : list) {
            PropertyDto dto = new PropertyDto();
            dto.setId(property.getId());
            dto.setTitle(property.getTitle());
            dto.setPrice(property.getPrice());
            dto.setAddress(property.getAddress());
            dto.setCity(property.getCity());
            dto.setMobileNumber(property.getMobileNumber());
            dto.setBhkType(property.getBhkType());
            dto.setLocation(property.getLocation());
            dto.setApartmentName(property.getApartmentName());
            attachDatabaseImages(dto, property.getId());
            dtoList.add(dto);
        }

        return dtoList;
    }

    @Override
    public List<PropertyDto> getPropertiesByOwnerId(Long ownerId) {

        List<Property> properties =
                propertyRepository.findByPropertyOwner_OwnerId(ownerId);

        List<PropertyDto> dtoList = new ArrayList<>();

        for (Property property : properties) {

            PropertyDto dto = new PropertyDto();

            dto.setId(property.getId());
            dto.setTitle(property.getTitle());
            dto.setPrice(property.getPrice());
            dto.setLocation(property.getLocation());
            dto.setAddress(property.getAddress());
            dto.setCity(property.getCity());
            dto.setState(property.getState());
            dto.setPincode(property.getPincode());
            dto.setDescription(property.getDescription());
            dto.setPropertyType(property.getPropertyType());
            dto.setPgType(property.getPgType());
            dto.setBhkType(property.getBhkType());
            dto.setFurnishing(property.getFurnishing());
            dto.setCarpetArea(property.getCarpetArea());
            dto.setMobileNumber(property.getMobileNumber());
            dto.setApartmentName(property.getApartmentName());
            dto.setStatus(property.getStatus());
            dto.setLikesCount(property.getLikesCount());
            dto.setViewsCount(property.getViewsCount());

            // PROPERTY PAYMENT STATUS
            dto.setPaymentStatus(property.getPaymentStatus());
            dto.setPremiumActive(property.isPremiumActive());

            // OWNER DETAILS
            PropertyOwner owner = property.getPropertyOwner();

            if (owner != null) {

                dto.setOwnerId(owner.getOwnerId());

                dto.setPremiumActive(owner.isPremiumActive());

                dto.setPremiumStatus(owner.getPremiumStatus());

                dto.setPremiumCount(owner.getPremiumCount());

            }

            attachDatabaseImages(dto, property.getId());

            dtoList.add(dto);
        }

        return dtoList;
    }

    @Override
    public String activateProperty(Long id) {
        Property property = propertyRepository.findById(id).orElse(null);
        if (property == null) {
            return MessageConfig.PROPERTY_NOT_FOUND;
        }
        property.setStatus(AppConstants.ACTIVE);
        propertyRepository.save(property);
        return "Property Activated Successfully";
    }
}