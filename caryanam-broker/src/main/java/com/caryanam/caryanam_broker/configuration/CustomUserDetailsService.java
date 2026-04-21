package com.caryanam.caryanam_broker.configuration;

import com.caryanam.caryanam_broker.entity.Admin;
import com.caryanam.caryanam_broker.entity.User;
import com.caryanam.caryanam_broker.repository.AdminRepository;
import com.caryanam.caryanam_broker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        //  Check USER
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {

            String role = user.getRole() != null ? user.getRole().name() : "USER";

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    List.of(new SimpleGrantedAuthority(role))
            );
        }

        //  Check ADMIN
        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin != null) {

            String role = admin.getRole() != null ? admin.getRole().name() : "ADMIN";

            return new org.springframework.security.core.userdetails.User(
                    admin.getEmail(),
                    admin.getPassword(),
                    List.of(new SimpleGrantedAuthority(role))
            );
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}