package com.caryanam.caryanam_broker.serviceimpl;

import com.caryanam.caryanam_broker.appconstant.AppConstants;
import com.caryanam.caryanam_broker.dto.PropertyDto;
import com.caryanam.caryanam_broker.dto.PropertyFilterDto;
import com.caryanam.caryanam_broker.entity.*;
import com.caryanam.caryanam_broker.messageconfig.MessageConfig;
import com.caryanam.caryanam_broker.repository.*;
import com.caryanam.caryanam_broker.service.PropertyService;
import jakarta.servlet.http.HttpServletRequest;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
        List<Property> properties = propertyRepository.findByStatus(AppConstants.ACTIVE);
        List<PropertyDto> dtoList = new ArrayList<>();
        for (Property property : properties) {
            PropertyDto dto = new PropertyDto();
            List<PropertyImage> images =
                    propertyImageRepository.findByPropertyId(property.getId());
            List<String> imageList = new ArrayList<>();
            for (PropertyImage img : images) {
                imageList.add(img.getImagePath());
            }
            if (!isPremium) {
                dto.setTitle(property.getTitle());
                dto.setPropertyType(property.getPropertyType());
                dto.setDoctypeImages(imageList.toString());
            }
            else {
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
                dto.setStatus(property.getStatus());
                dto.setDoctypeImages(imageList.toString());
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
        dto.setStatus(property.getStatus());
        List<PropertyImage> imageList = propertyImageRepository.findByPropertyId(id);
        List<String> doctypeImages = new ArrayList<>();
        if (imageList != null && imageList.size() > 0) {
            for (int i = 0; i < imageList.size(); i++) {
                String path = imageList.get(i).getImagePath();
                if (i == 0) {
                    dto.setCoverImage(path);
                } else {
                    doctypeImages.add(path);
                }
            }
        }
        dto.setDoctypeImages(String.valueOf(doctypeImages));
        return dto;
    }

    @Override
    public PropertyDto updateProperty(Long id, PropertyDto propertyDto) {
        Property property = propertyRepository.findById(id).orElse(null);
        if (property == null) {
            return null;
        }
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
        responseDto.setStatus(updatedProperty.getStatus());
        responseDto.setLikesCount(updatedProperty.getLikesCount());
        responseDto.setViewsCount(updatedProperty.getViewsCount());
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
        String uploadDir = System.getProperty(AppConstants.USER_DIR) + AppConstants.UPLOAD_DIR;
        java.io.File folder = new java.io.File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        int index = 0;
        StringBuilder doctypeImages = new StringBuilder();
        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalName;
            String filePath = uploadDir + fileName;
            Long originalKb = file.getSize() / 1024;
            Double originalMb = file.getSize() / (1024.0 * 1024.0);
            try {
                java.io.File compressedFile = new java.io.File(filePath);
                Thumbnails.of(file.getInputStream())
                        .scale(1.0)
                        .outputQuality(0.5)
                        .toFile(compressedFile);
                Long compressedKb = compressedFile.length() / 1024;
                Double compressedMb = compressedFile.length() / (1024.0 * 1024.0);
                PropertyImage image = new PropertyImage();
                image.setImageName(fileName);
                image.setImagePath(fileName);
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
    public List<PropertyDto> filterProperties(PropertyFilterDto filterDto, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isPremiumActive()) {
            return new ArrayList<>();
        }
        List<Property> allProperties = propertyRepository.findAll();
        List<Property> filteredList = new ArrayList<>();
        for (Property property : allProperties) {
            boolean match = true;
            if (filterDto.getPropertyType() != null
                    && !filterDto.getPropertyType().isEmpty()
                    && !filterDto.getPropertyType().equalsIgnoreCase("ALL")) {

                if (!property.getPropertyType().name().equalsIgnoreCase(filterDto.getPropertyType())) {
                    match = false;
                }
            }
            if (filterDto.getMinPrice() != null) {
                if (property.getPrice() < filterDto.getMinPrice()) {
                    match = false;
                }
            }

            if (filterDto.getMaxPrice() != null) {
                if (property.getPrice() > filterDto.getMaxPrice()) {
                    match = false;
                }
            }

            if (match) {
                filteredList.add(property);
            }
        }
        if (filterDto.getSortBy() != null) {
            for (int i = 0; i < filteredList.size(); i++) {
                for (int j = i + 1; j < filteredList.size(); j++) {
                    if (filterDto.getSortBy().equalsIgnoreCase(AppConstants.ASC)) {
                        if (filteredList.get(i).getPrice() > filteredList.get(j).getPrice()) {
                            Property temp = filteredList.get(i);
                            filteredList.set(i, filteredList.get(j));
                            filteredList.set(j, temp);
                        }
                    }
                    if (filterDto.getSortBy().equalsIgnoreCase(AppConstants.DESC)) {
                        if (filteredList.get(i).getPrice() < filteredList.get(j).getPrice()) {
                            Property temp = filteredList.get(i);
                            filteredList.set(i, filteredList.get(j));
                            filteredList.set(j, temp);
                        }
                    }
                }
            }
        }
        List<PropertyDto> dtoList = new ArrayList<>();
        for (Property property : filteredList) {
            PropertyDto dto = new PropertyDto();
            dto.setId(property.getId());
            dto.setTitle(property.getTitle());
            dto.setPrice(property.getPrice());
            dto.setLocation(property.getLocation());
            dto.setDescription(property.getDescription());
            dto.setPropertyType(property.getPropertyType());
            dto.setPgType(property.getPgType());
            dto.setBhkType(property.getBhkType());
            dto.setFurnishing(property.getFurnishing());
            dto.setCarpetArea(property.getCarpetArea());
            dto.setMobileNumber(property.getMobileNumber());
            dto.setLikesCount(property.getLikesCount());
            dto.setViewsCount(property.getViewsCount());
            dto.setStatus(property.getStatus());
            List<PropertyImage> images = propertyImageRepository.findByPropertyId(property.getId());
            List<String> imageList = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                String path = images.get(i).getImagePath();
                if (i == 0) {
                    dto.setCoverImage(path);
                } else {
                    imageList.add(path);
                }
            }
            dto.setDoctypeImages(imageList.toString());
            dtoList.add(dto);
        }
        return dtoList;
    }

    public List<PropertyDto> getPropertiesByCityAndAddress(String city, String address) {
        List<Property> list;
        if (address == null || address.isEmpty()) {
            list = propertyRepository.findByCityIgnoreCase(city);
        } else {
            list = propertyRepository
                    .findByCityIgnoreCaseAndAddressIgnoreCase(city, address);
        }
        List<PropertyDto> dtoList = new ArrayList<>();
        for (Property property : list) {
            PropertyDto dto = new PropertyDto();
            dto.setId(property.getId());
            dto.setTitle(property.getTitle());
            dto.setPrice(property.getPrice());
            dto.setAddress(property.getAddress());
            dto.setCity(property.getCity());
            dtoList.add(dto);
        }
        return dtoList;
    }

    public List<String> getAddressesByCity(String city) {
        List<Property> list = propertyRepository.findByCityIgnoreCase(city);
        return list.stream()
                .map(Property::getAddress)
                .distinct()
                .toList();
    }
}