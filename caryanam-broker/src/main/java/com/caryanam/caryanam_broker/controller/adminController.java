package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.entity.Admin;
import com.caryanam.caryanam_broker.entity.User;
import com.caryanam.caryanam_broker.repository.AdminRepository;
import com.caryanam.caryanam_broker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/admin")
public class adminController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/pending-users")
    public List<User> getPendingUsers() {
        return userRepository.findByPremiumStatus("PENDING");
    }

    @GetMapping("/pending-admins")
    public List<Admin> getPendingAdmins() {
        return adminRepository.findByPremiumStatus("PENDING");
    }

    @PostMapping("/approvePremium")
    public ResponseEntity<String> approvePremium(@RequestParam String type, @RequestParam Long id) {
        if (type.equalsIgnoreCase("USER")) {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            user.setPremiumActive(true);
            user.setPremiumStatus("APPROVED");
            userRepository.save(user);
            return ResponseEntity.ok("User premium approved");
        } else if (type.equalsIgnoreCase("ADMIN")) {
            Admin admin = adminRepository.findById(id).orElse(null);
            if (admin == null) {
                return ResponseEntity.badRequest().body("Admin not found");
            }
            admin.setPremiumActive(true);
            admin.setPremiumStatus("APPROVED");
            adminRepository.save(admin);
            return ResponseEntity.ok("Admin premium approved");
        }
        return ResponseEntity.badRequest().body("Invalid type");
    }
}