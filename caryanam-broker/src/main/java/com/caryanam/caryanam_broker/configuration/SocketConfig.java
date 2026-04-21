package com.caryanam.caryanam_broker.configuration;

import com.caryanam.caryanam_broker.repository.UserRepository;
import com.caryanam.caryanam_broker.repository.AdminRepository;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.AuthorizationResult;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
@RequiredArgsConstructor
public class SocketConfig {

    private final UserRepository userRepo;
    private final AdminRepository adminRepo;

    @Bean
    public SocketIOServer socketIOServer() {


        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();

        config.setHostname("localhost");
        config.setPort(9092);
        config.setOrigin("*");


        config.setAuthorizationListener(data -> {

            String userIdStr = data.getSingleUrlParam("userId");
            String adminIdStr = data.getSingleUrlParam("adminId");


            if (userIdStr != null && adminIdStr != null) {
                System.out.println(" BOTH userId & adminId present");
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            if (userIdStr == null && adminIdStr == null) {
                System.out.println(" No ID provided");
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }


            if (userIdStr != null) {
                try {
                    Long userId = Long.valueOf(userIdStr);

                    if (userRepo.existsById(userId)) {
                        System.out.println(" Connected USER: " + userId);
                        return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
                    }

                    System.out.println(" USER not found: " + userId);

                } catch (Exception e) {
                    System.out.println(" Invalid USER ID");
                }
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }


            if (adminIdStr != null) {
                try {
                    Long adminId = Long.valueOf(adminIdStr);

                    if (adminRepo.existsById(adminId)) {
                        System.out.println(" Connected ADMIN: " + adminId);
                        return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
                    }

                    System.out.println("ADMIN not found: " + adminId);

                } catch (Exception e) {
                    System.out.println(" Invalid ADMIN ID");
                }
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            return AuthorizationResult.FAILED_AUTHORIZATION;
        });

        return new SocketIOServer(config);
    }
}