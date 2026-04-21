package com.caryanam.caryanam_broker.configuration;

import com.caryanam.caryanam_broker.service.ChatService;
import com.caryanam.caryanam_broker.socket.MessageRequestDTO;
import com.caryanam.caryanam_broker.socket.MessageResponseDTO;
import com.corundumstudio.socketio.*;
        import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketModule {

    private final SocketIOServer server;
    private final ChatService chatService;

    @PostConstruct
    public void init() {


        server.addConnectListener(client -> {
            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            String adminId = client.getHandshakeData().getSingleUrlParam("adminId");

            if (userId != null) {
                System.out.println(" USER Connected: " + userId);
            } else if (adminId != null) {
                System.out.println(" ADMIN Connected: " + adminId);
            }
        });


        server.addEventListener("join_room", String.class, (client, roomId, ackSender) -> {

            client.joinRoom(roomId);

            System.out.println(" Joined Room: " + roomId);

            int count = server.getRoomOperations(roomId).getClients().size();
            System.out.println(" TOTAL CLIENTS IN ROOM: " + count);
        });

        server.addEventListener("send_message", MessageRequestDTO.class, (client, dto, ackSender) -> {

            System.out.println(" Message received: " + dto);

            MessageResponseDTO response = chatService.sendMessage(dto);

            String roomId = response.getRoomId();

            System.out.println(" Sending to ROOM: " + roomId);

            server.getRoomOperations(roomId)
                    .sendEvent("receive_message", response);
        });

        //server.start();
    }
}