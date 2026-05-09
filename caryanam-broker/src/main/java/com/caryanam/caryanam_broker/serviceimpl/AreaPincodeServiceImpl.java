package com.caryanam.caryanam_broker.serviceimpl;

import com.caryanam.caryanam_broker.entity.AreaPincode;
import com.caryanam.caryanam_broker.repository.AreaPincodeRepository;
import com.caryanam.caryanam_broker.service.AreaPincodeService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

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
                AreaPincode existing = areaPincodeRepository.findByCityIgnoreCaseAndAreaIgnoreCase(city, area);
                if (existing != null) {
                    continue;
                }
                AreaPincode areaPincode = new AreaPincode();
                areaPincode.setCity(city);
                areaPincode.setArea(area);
                areaPincode.setPincode(pincode);
                areaPincodeRepository.save(areaPincode);
            }
            workbook.close();
            return "Excel uploaded successfully";

        } catch (Exception e) {

            e.printStackTrace();
            return "Failed to upload excel";
        }
    }
}