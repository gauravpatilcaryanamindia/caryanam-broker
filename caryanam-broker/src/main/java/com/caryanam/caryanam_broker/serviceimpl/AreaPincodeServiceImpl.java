

package com.caryanam.caryanam_broker.serviceimpl;

import com.caryanam.caryanam_broker.entity.AreaPincode;
import com.caryanam.caryanam_broker.repository.AreaPincodeRepository;
import com.caryanam.caryanam_broker.service.AreaPincodeService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class AreaPincodeServiceImpl implements AreaPincodeService {

    @Autowired
    private AreaPincodeRepository areaPincodeRepository;

    @Override
    public String uploadExcel(MultipartFile file) {

        try {

            InputStream inputStream = file.getInputStream();

            Workbook workbook = WorkbookFactory.create(inputStream);

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {

                if (row.getRowNum() == 0) {
                    continue;
                }

                Cell cityCell = row.getCell(0);
                Cell areaCell = row.getCell(1);
                Cell pincodeCell = row.getCell(2);
                Cell nearbyCell = row.getCell(3);
                Cell nearbyPincodeCell = row.getCell(4);

                if (cityCell == null || areaCell == null || pincodeCell == null) {
                    continue;
                }

                String city = cityCell.getStringCellValue().trim();

                String area = areaCell.getStringCellValue().trim();

                String pincode;

                if (pincodeCell.getCellType() == CellType.NUMERIC) {

                    pincode = String.valueOf((long) pincodeCell.getNumericCellValue());

                } else {

                    pincode = pincodeCell.getStringCellValue().trim();
                }

                String nearby = "";

                if (nearbyCell != null) {

                    nearby = nearbyCell.getStringCellValue().trim();
                }

                String nearbyPincode = "";

                if (nearbyPincodeCell != null) {

                    if (nearbyPincodeCell.getCellType() == CellType.NUMERIC) {

                        nearbyPincode = String.valueOf(
                                (long) nearbyPincodeCell.getNumericCellValue());

                    } else {

                        nearbyPincode = nearbyPincodeCell.getStringCellValue().trim();
                    }
                }

                AreaPincode existing =
                        areaPincodeRepository.findByCityIgnoreCaseAndAreaIgnoreCase(city, area);

                if (existing != null) {
                    continue;
                }

                AreaPincode areaPincode = new AreaPincode();

                areaPincode.setCity(city);
                areaPincode.setArea(area);
                areaPincode.setPincode(pincode);

                areaPincode.setNearBy(nearby);
                areaPincode.setNearbyPincode(nearbyPincode);

                areaPincodeRepository.save(areaPincode);
            }

            workbook.close();

            return "Excel uploaded successfully";

        } catch (Exception e) {

            e.printStackTrace();

            return "Failed to upload excel";
        }
    }
//
//    @Override
//    public List<String> getNearbyData(String nearbyPincode) {
//
//        List<AreaPincode> list =
//                areaPincodeRepository.findByNearbyPincode(nearbyPincode);
//
//        List<String> response = new ArrayList<>();
//
//        for (AreaPincode areaPincode : list) {
//
//            if (areaPincode.getNearBy() != null &&
//                    !areaPincode.getNearBy().isEmpty()) {
//
//                String[] nearbyArray =
//                        areaPincode.getNearBy().split(",");
//
//                for (String nearby : nearbyArray) {
//
//                    response.add(nearby.trim());
//                }
//            }
//        }
//
//        return response;
//    }

    @Override
    public List<String> getNearbyData(String nearbyPincode) {

        if (nearbyPincode == null || nearbyPincode.isBlank()) {
            return new ArrayList<>();
        }

        String cleanedPincode = nearbyPincode.trim();

        List<AreaPincode> list =
                areaPincodeRepository.findByNearbyPincode(cleanedPincode);

        List<String> response = new ArrayList<>();

        for (AreaPincode areaPincode : list) {

            if (areaPincode.getNearBy() != null &&
                    !areaPincode.getNearBy().isBlank()) {

                String[] nearbyArray =
                        areaPincode.getNearBy().split(",");

                for (String nearby : nearbyArray) {
                    response.add(nearby.trim());
                }
            }
        }
        System.out.println("Pincode from frontend = " + nearbyPincode);
        System.out.println("Matched rows = " + list.size());
        return response;
    }
}