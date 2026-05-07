package com.caryanam.caryanam_broker.service;

import org.springframework.web.multipart.MultipartFile;

public interface AreaPincodeService {

    String uploadExcel(MultipartFile file);
}