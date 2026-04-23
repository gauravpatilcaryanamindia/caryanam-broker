package com.caryanam.caryanam_broker.serviceimpl;


import com.caryanam.caryanam_broker.configuration.JwtUtil;
import com.caryanam.caryanam_broker.dto.LoginRequestDTO;
import com.caryanam.caryanam_broker.dto.RegisterRequestDTO;
import com.caryanam.caryanam_broker.dto.RegisterResponseDTO;
import com.caryanam.caryanam_broker.entity.Admin;
import com.caryanam.caryanam_broker.entity.User;
import com.caryanam.caryanam_broker.enums.Role;
import com.caryanam.caryanam_broker.repository.AdminRepository;
import com.caryanam.caryanam_broker.repository.UserRepository;
import com.caryanam.caryanam_broker.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

     @Autowired
    private AuthenticationManager authenticationManager;

    private static final Set<String> tokenBlacklist = new HashSet<>();

    //  USER REGISTRATION
    @Override
    public RegisterResponseDTO registerUser(RegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("User email already exists");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setMobileNumber(String.valueOf(dto.getMobileNumber()));
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);

        return RegisterResponseDTO.builder()
                .id(saved.getUserId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .role(saved.getRole().name())

                .build();
    }

    // ADMIN REGISTRATION
    @Override
    public RegisterResponseDTO registerAdmin(RegisterRequestDTO dto) {

        if (adminRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Admin email already exists");
        }

        Admin admin = new Admin();
        admin.setFullName(dto.getFullName());
        admin.setMobileNumber(String.valueOf(dto.getMobileNumber()));
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setRole(Role.ADMIN);

        Admin saved = adminRepository.save(admin);

        return RegisterResponseDTO.builder()
                .id(saved.getAdminId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .role(saved.getRole().name())

                .build();
    }

//    //login USER,ADMIN
//    @Override
//    public String login(LoginRequestDTO request) {
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );
//
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//
//        String role = userDetails.getAuthorities()
//                .stream()
//                .findFirst()
//                .map(granted -> granted.getAuthority())
//                .orElse("USER");
//
//        return jwtUtil.generateToken(
//                userDetails.getUsername(),
//                role
//        );
//    }

    @Override
    public String login(LoginRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String role = userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(granted -> granted.getAuthority())
                .orElse("USER");

        // ⭐ ADD DEVICE TYPE (IMPORTANT FIX)
        String deviceType = request.getDeviceType();

        if (deviceType == null || deviceType.isEmpty()) {
            deviceType = "WEB";   // fallback
        }

        return jwtUtil.generateToken(
                userDetails.getUsername(),
                role,
                deviceType
        );
    }

    @Override
    public void logout(String token) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        tokenBlacklist.add(token);

        System.out.println("User logged out, token blacklisted: " + token);
    }

    public static boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

}
