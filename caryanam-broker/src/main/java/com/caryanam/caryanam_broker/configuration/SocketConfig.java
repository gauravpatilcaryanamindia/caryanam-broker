package com.caryanam.caryanam_broker.configuration;

import com.caryanam.caryanam_broker.repository.PropertyOwnerRepository;
import com.caryanam.caryanam_broker.repository.UserRepository;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.AuthorizationResult;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
@RequiredArgsConstructor
public class SocketConfig {

    private final UserRepository userRepo;
    private final PropertyOwnerRepository ownerRepo;

    @Bean
    public SocketIOServer socketIOServer() {


        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();

        config.setHostname("localhost");
        config.setPort(9092);
        config.setOrigin("*");


        config.setAuthorizationListener(data -> {

            String userIdStr = data.getSingleUrlParam("userId");
            String ownerIdStr = data.getSingleUrlParam("ownerId");


            if (userIdStr != null && ownerIdStr != null) {
                System.out.println(" BOTH userId & ownerId present");
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            if (userIdStr == null && ownerIdStr == null) {
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


            if (ownerIdStr != null) {
                try {
                    Long ownerId = Long.valueOf(ownerIdStr);

                    if (ownerRepo.existsById(ownerId)) {
                        System.out.println(" Connected PROPERTY OWNER: " + ownerId);
                        return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
                    }

                    System.out.println("OWNER not found: " + ownerId);

                } catch (Exception e) {
                    System.out.println(" Invalid OWNER ID");
                }
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            return AuthorizationResult.FAILED_AUTHORIZATION;
        });

        return new SocketIOServer(config);
    }
}