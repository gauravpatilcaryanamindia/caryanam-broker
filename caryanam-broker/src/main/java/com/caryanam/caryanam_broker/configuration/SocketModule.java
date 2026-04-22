package com.caryanam.caryanam_broker.configuration;

import com.caryanam.caryanam_broker.service.ChatService;
import com.caryanam.caryanam_broker.socket.MessageRequestDTO;
import com.caryanam.caryanam_broker.socket.MessageResponseDTO;
import com.caryanam.caryanam_broker.socket.TypingDTO;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class SocketModule {

    private final SocketIOServer server;
    private final ChatService chatService;

    public SocketModule(SocketIOServer server, ChatService chatService) {
        this.server = server;
        this.chatService = chatService;
    }

    @PostConstruct
    public void init() {


        server.addConnectListener(client -> {
            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            String adminId = client.getHandshakeData().getSingleUrlParam("adminId");

            if (userId != null) {
                System.out.println("USER Connected: " + userId);
            } else if (adminId != null) {
                System.out.println("ADMIN Connected: " + adminId);
            }

            System.out.println("Session ID: " + client.getSessionId());
        });


        server.addDisconnectListener(client -> {
            System.out.println("Client Disconnected: " + client.getSessionId());
        });


        server.addEventListener("join_room", String.class, (client, roomId, ackSender) -> {

            // Prevent duplicate joins
            client.leaveRoom(roomId);
            client.joinRoom(roomId);

            System.out.println("Joined Room: " + roomId);

            int count = server.getRoomOperations(roomId).getClients().size();
            System.out.println("TOTAL CLIENTS IN ROOM: " + count);
        });


        server.addEventListener("send_message", MessageRequestDTO.class, (client, dto, ackSender) -> {

            System.out.println("Message received: " + dto);

            // Call service
            MessageResponseDTO response = chatService.sendMessage(dto);

            String roomId = response.getRoomId();

            System.out.println("Broadcasting to ROOM: " + roomId);


            server.getRoomOperations(roomId).sendEvent("receive_message", response);
        });


        server.addEventListener("typing", TypingDTO.class, (client, dto, ackSender) -> {

            System.out.println("Typing event: " + dto);

            if (dto.getRoomId() == null) {
                System.out.println("roomId is null");
                return;
            }

            for (SocketIOClient c : server.getRoomOperations(dto.getRoomId()).getClients()) {
                if (!c.getSessionId().equals(client.getSessionId())) {
                    c.sendEvent("typing", dto);
                }
            }


            chatService.handleTyping(dto);
        });
    }
}