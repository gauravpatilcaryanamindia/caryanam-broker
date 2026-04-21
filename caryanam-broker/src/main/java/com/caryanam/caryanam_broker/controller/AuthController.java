package com.caryanam.caryanam_broker.controller;


import com.caryanam.caryanam_broker.dto.*;
import com.caryanam.caryanam_broker.service.AuthService;

import lombok.RequiredArgsConstructor;
import com.caryanam.caryanam_broker.enums.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //  USER REGISTER
    @PostMapping("/register/user")
    public ResponseEntity<ResponseDto<RegisterResponseDTO>> registerUser(
            @RequestBody RegisterRequestDTO dto) {

        //  Null check
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Request body is missing", null));
        }

        // Full Name validation
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Full name is required", null));
        }

        // Regex: Only alphabets + spaces allowed
        if (!dto.getFullName().matches("^[A-Za-z ]+$")) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Full name must contain only letters and spaces", null));
        }

        //  Mobile Number
        if (dto.getMobileNumber() == null || !dto.getMobileNumber().matches("\\d{10}")) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Mobile number must be 10 digits", null));
        }

        //  Email (ONLY Gmail + lowercase)
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Email is required", null));
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Only Gmail format allowed (example: user@gmail.com)", null));
        }

        //  Password
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Password must be at least 6 characters", null));
        }

        //  Role
        if (dto.getRole() == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Role is required", null));
        }

        //  Normalize email (important)
        dto.setEmail(dto.getEmail().toLowerCase().trim());

        //  Call service
        RegisterResponseDTO response = authService.registerUser(dto);

        return ResponseEntity.status(201)
                .body(new ResponseDto<>(201, "User Registered Successfully", response));
    }

    //  ADMIN REGISTER
    @PostMapping("/register/admin")
    public ResponseEntity<ResponseDto<RegisterResponseDTO>> registerAdmin(
            @RequestBody RegisterRequestDTO dto) {

        // Null check
        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Request body is missing", null));
        }

        // Full Name
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Full name is required", null));
        }

        // Mobile Number
        if (dto.getMobileNumber() == null || !dto.getMobileNumber().matches("\\d{10}")) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Mobile number must be 10 digits", null));
        }

        //  Email (ONLY lowercase Gmail)
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Email is required", null));
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Only Gmail format allowed", null));
        }

        // Password
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Password must be at least 6 characters", null));
        }

        // Role (STRICT ADMIN ONLY)
        if (dto.getRole() == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Role is required", null));
        }

        if (dto.getRole() != Role.ADMIN) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(400, "Only ADMIN role allowed", null));
        }

        // Normalize email
        dto.setEmail(dto.getEmail().toLowerCase().trim());

        //  Call service
        RegisterResponseDTO response = authService.registerAdmin(dto);

        return ResponseEntity.status(201)
                .body(new ResponseDto<>(201, "Admin Registered Successfully", response));
    }


    //LOGIN USER,ADMIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO request) {

        // Null check
        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponseDTO(400, "Request body is missing", null));
        }

        // Email validation
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponseDTO(400, "Email is required", null));
        }

        // Strict email (lowercase gmail only)
        if (!request.getEmail().matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponseDTO(400, "Invalid email (only gmail format allowed)", null));
        }

        // Password validation
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponseDTO(400, "Password is required", null));
        }

        if (request.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponseDTO(400, "Password must be at least 6 characters", null));
        }

        // Normalize email
        request.setEmail(request.getEmail().toLowerCase().trim());

        //  Call service
        String token = authService.login(request);

        return ResponseEntity.ok(
                new LoginResponseDTO(200, "Login Successful", token)
        );
    }

}
