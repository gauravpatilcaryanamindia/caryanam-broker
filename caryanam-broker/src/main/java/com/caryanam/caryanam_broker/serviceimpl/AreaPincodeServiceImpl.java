//package com.caryanam.caryanam_broker.serviceimpl;
//
//import com.caryanam.caryanam_broker.entity.AreaPincode;
//import com.caryanam.caryanam_broker.repository.AreaPincodeRepository;
//import com.caryanam.caryanam_broker.service.AreaPincodeService;
//import org.apache.poi.ss.usermodel.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.InputStream;
//import java.util.Iterator;
//
//@Service
//public class AreaPincodeServiceImpl implements AreaPincodeService {
//
//    @Autowired
//    private AreaPincodeRepository areaPincodeRepository;
//
//    @Override
//    public String uploadExcel(MultipartFile file) {
//
//        try {
//
//            InputStream inputStream = file.getInputStream();
//
//            Workbook workbook = WorkbookFactory.create(inputStream);
//
//            Sheet sheet = workbook.getSheetAt(0);
//
//            Iterator<Row> rows = sheet.iterator();
//
//            int rowNumber = 0;
//
//            while (rows.hasNext()) {
//
//                Row row = rows.next();
//
//                // Skip Header
//                if (rowNumber == 0) {
//                    rowNumber++;
//                    continue;
//                }
//
//                // ===== Pune Data =====
//
//                String puneArea = getCellValue(row.getCell(0));
//                String punePincode = getCellValue(row.getCell(1));
//                String puneNearBy = getCellValue(row.getCell(2));
//
//                if (puneArea != null && !puneArea.isEmpty()) {
//
//                    AreaPincode pune = new AreaPincode();
//
//                    pune.setCity("Pune");
//                    pune.setArea(puneArea);
//                    pune.setPincode(punePincode);
//                    pune.setNearBy(puneNearBy);
//
//                    areaPincodeRepository.save(pune);
//                }
//
//                // ===== PCMC Data =====
//
//                String pcmcArea = getCellValue(row.getCell(3));
//                String pcmcPincode = getCellValue(row.getCell(4));
//                String pcmcNearBy = getCellValue(row.getCell(5));
//
//                if (pcmcArea != null && !pcmcArea.isEmpty()) {
//
//                    AreaPincode pcmc = new AreaPincode();
//
//                    pcmc.setCity("PCMC");
//                    pcmc.setArea(pcmcArea);
//                    pcmc.setPincode(pcmcPincode);
//                    pcmc.setNearBy(pcmcNearBy);
//
//                    areaPincodeRepository.save(pcmc);
//                }
//            }
//
//            workbook.close();
//
//            return "Excel uploaded successfully";
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//            return "Excel upload failed";
//        }
//    }
//
//    private String getCellValue(Cell cell) {
//
//        if (cell == null) {
//            return "";
//        }
//
//        switch (cell.getCellType()) {
//
//            case STRING:
//                return cell.getStringCellValue().trim();
//
//            case NUMERIC:
//                return String.valueOf((long) cell.getNumericCellValue());
//
//            default:
//                return "";
//        }
//    }
//}

package com.caryanam.caryanam_broker.serviceimpl;

import com.caryanam.caryanam_broker.entity.AreaPincode;
import com.caryanam.caryanam_broker.repository.AreaPincodeRepository;
import com.caryanam.caryanam_broker.service.AreaPincodeService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Iterator;

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

            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;

            while (rows.hasNext()) {

                Row row = rows.next();

                // Skip Header Row
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                String city = getCellValue(row.getCell(0));
                String area = getCellValue(row.getCell(1));
                String pincode = getCellValue(row.getCell(2));
                String nearBy = getCellValue(row.getCell(3));

                if (area != null && !area.isEmpty()) {

                    AreaPincode areaPincode = new AreaPincode();

                    areaPincode.setCity(city);
                    areaPincode.setArea(area);
                    areaPincode.setPincode(pincode);
                    areaPincode.setNearBy(nearBy);

                    areaPincodeRepository.save(areaPincode);
                }
            }

            workbook.close();

            return "Excel uploaded successfully";

        } catch (Exception e) {

            e.printStackTrace();

            return "Excel upload failed";
        }
    }

    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());

            default:
                return "";
        }
    }
}