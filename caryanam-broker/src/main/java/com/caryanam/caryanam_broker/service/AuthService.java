package com.caryanam.caryanam_broker.service;
import com.caryanam.caryanam_broker.dto.LoginRequestDTO;
import com.caryanam.caryanam_broker.dto.RegisterRequestDTO;
import com.caryanam.caryanam_broker.dto.RegisterResponseDTO;

public interface AuthService {

    RegisterResponseDTO registerUser(RegisterRequestDTO dto);

    RegisterResponseDTO registerAdmin(RegisterRequestDTO dto);

    RegisterResponseDTO registerPropertyOwner(RegisterRequestDTO dto);

    public String login(LoginRequestDTO dto);

     void logout(String token);
}
