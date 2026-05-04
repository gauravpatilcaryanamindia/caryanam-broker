package com.caryanam.caryanam_broker.serviceimpl;


import com.caryanam.caryanam_broker.configuration.JwtUtil;
import com.caryanam.caryanam_broker.dto.LoginRequestDTO;
import com.caryanam.caryanam_broker.dto.RegisterRequestDTO;
import com.caryanam.caryanam_broker.dto.RegisterResponseDTO;
import com.caryanam.caryanam_broker.entity.Admin;
import com.caryanam.caryanam_broker.entity.PropertyOwner;
import com.caryanam.caryanam_broker.entity.User;
import com.caryanam.caryanam_broker.enums.Role;
import com.caryanam.caryanam_broker.repository.AdminRepository;
import com.caryanam.caryanam_broker.repository.PropertyOwnerRepository;
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
    private PropertyOwnerRepository propertyOwerRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private PropertyOwnerRepository propertyOwnerRepository;


    private static final Set<String> tokenBlacklist = new HashSet<>();

    //  USER REGISTRATION
    @Override
    public RegisterResponseDTO registerUser(RegisterRequestDTO dto) {

        if (isEmailAlreadyUsed(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setMobileNumber(String.valueOf(dto.getMobileNumber()));
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive("true");
        User saved = userRepository.save(user);

        return RegisterResponseDTO.builder()
                .id(saved.getUserId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .role(saved.getRole().name())

                .build();
    }

    @Override
    public RegisterResponseDTO registerPropertyOwner(RegisterRequestDTO dto) {
        if (isEmailAlreadyUsed(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        PropertyOwner owner = new PropertyOwner();
        owner.setFullName(dto.getFullName());
        owner.setMobileNumber(String.valueOf(dto.getMobileNumber()));
        owner.setEmail(dto.getEmail());
        owner.setEmail(dto.getEmail());
        owner.setPassword(passwordEncoder.encode(dto.getPassword()));
        owner.setRole(Role.PROPERTY_OWNER);
        owner.setIsActive("true");
        PropertyOwner saved = propertyOwerRepository.save(owner);
        return RegisterResponseDTO.builder()
                .id(saved.getOwnerId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }


    @Override
    public String login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(granted -> granted.getAuthority())
                .orElse("USER");

        Long id = null;
        if ("ROLE_USER".equals(role)) {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                id = user.getUserId();
            }
        }
        else if ("ROLE_PROPERTY_OWNER".equals(role)) {
            PropertyOwner owner = propertyOwnerRepository.findByEmail(request.getEmail()).orElse(null);
            if (owner != null) {
                id = owner.getOwnerId();
            }
        }
        else if ("ROLE_ADMIN".equals(role)) {
            Admin admin = adminRepository.findByEmail(request.getEmail()).orElse(null);
            if (admin != null) {
                id = admin.getAdminId();
            }
        }
        String deviceType = request.getDeviceType();
        if (deviceType == null || deviceType.isEmpty()) {
            deviceType = "WEB";
        }
        return jwtUtil.generateToken(
                userDetails.getUsername(),
                role,
                deviceType,
                id
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

    public Object updateUser(Long id, RegisterRequestDTO dto) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return null;
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getMobileNumber() != null) user.setMobileNumber(dto.getMobileNumber());
        if (dto.getEmail() != null) {
            if (!dto.getEmail().equals(user.getEmail()) && isEmailAlreadyUsed(dto.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(dto.getEmail());
        }        if (dto.getPassword() != null) user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
        return user;
    }

    public Object updateOwner(Long id, RegisterRequestDTO dto) {
        PropertyOwner propertyOwner = propertyOwerRepository.findById(id).orElse(null);
        if (propertyOwner == null) return null;
        if (dto.getFullName() != null) propertyOwner.setFullName(dto.getFullName());
        if (dto.getMobileNumber() != null) propertyOwner.setMobileNumber(dto.getMobileNumber());
        if (dto.getEmail() != null) {
            if (!dto.getEmail().equals(propertyOwner.getEmail()) && isEmailAlreadyUsed(dto.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            propertyOwner.setEmail(dto.getEmail());
        }        if (dto.getPassword() != null) propertyOwner.setPassword(passwordEncoder.encode(dto.getPassword()));
        propertyOwerRepository.save(propertyOwner);
        return propertyOwner;
    }

    public Object updateAdmin(Long id, RegisterRequestDTO dto) {
        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin == null) return null;
        if (dto.getFullName() != null) admin.setFullName(dto.getFullName());
        if (dto.getMobileNumber() != null) admin.setMobileNumber(dto.getMobileNumber());
        if (dto.getEmail() != null) {
            if (!dto.getEmail().equals(admin.getEmail()) && isEmailAlreadyUsed(dto.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            admin.setEmail(dto.getEmail());
        }        if (dto.getPassword() != null) admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        adminRepository.save(admin);
        return admin;
    }

    public boolean deactivateUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return false;
        user.setIsActive("false");
        userRepository.save(user);
        return true;
    }

    public boolean deactivateOwner(Long id) {
        PropertyOwner propertyOwner = propertyOwerRepository.findById(id).orElse(null);
        if (propertyOwner == null) return false;
        propertyOwner.setIsActive("false");
        propertyOwerRepository.save(propertyOwner);
        return true;
    }

    public boolean deactivateAdmin(Long id) {
        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin == null) return false;
        admin.setIsActive("false");
        adminRepository.save(admin);
        return true;
    }

    private boolean isEmailAlreadyUsed(String email) {
        return userRepository.existsByEmail(email)
                || propertyOwerRepository.existsByEmail(email)
                || adminRepository.existsByEmail(email);
    }
}
