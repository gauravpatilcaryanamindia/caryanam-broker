package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.dto.ResponseDto;
import com.caryanam.caryanam_broker.exception.BadRequestException;
import com.caryanam.caryanam_broker.service.ChatService;
import com.caryanam.caryanam_broker.socket.MessageRequestDTO;
import com.caryanam.caryanam_broker.socket.MessageResponseDTO;


import com.caryanam.caryanam_broker.socket.RoomRequestDTO;
import com.corundumstudio.socketio.SocketIOServer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    //  SEND MESSAGE
    @PostMapping("/send")
    public ResponseEntity<ResponseDto<MessageResponseDTO>> send(
            @Valid @RequestBody MessageRequestDTO dto) {

        if (dto.getSenderId() == null || dto.getReceiverId() == null) {
            throw new BadRequestException("SenderId and ReceiverId are required");
        }

        if (dto.getSenderId().equals(dto.getReceiverId())) {
            throw new BadRequestException("Sender and Receiver cannot be same");
        }

        // ================= AUTO FIRST MESSAGE =================
        dto.setMessage("Hi, I’m interested in your property. Could you please share more information?");
        MessageResponseDTO userMsg = chatService.sendMessage(dto);

        // ================= AUTO ADMIN REPLY =================
        MessageRequestDTO adminDto = new MessageRequestDTO();
        adminDto.setSenderId(dto.getReceiverId());
        adminDto.setReceiverId(dto.getSenderId());
        adminDto.setMessage("Please wait, an agent will connect with you shortly.");

        chatService.sendMessage(adminDto);

        boolean isAccepted = chatService.isChatAccepted(userMsg.getRoomId());

        return ResponseEntity.ok(
                new ResponseDto<>(
                        200,
                        "Auto messages sent",
                        userMsg
                )
        );
    }

    //  CREATE ROOM
    @PostMapping("/room")
    public ResponseEntity<ResponseDto<String>> createRoom(
            @Valid @RequestBody RoomRequestDTO request) {

        if (request.getUserId() == null || request.getAdminId() == null) {
            throw new BadRequestException("UserId and AdminId are required");
        }

        if (request.getUserId().equals(request.getAdminId())) {
            throw new BadRequestException("User and Admin cannot be same");
        }

        String roomId = chatService.createOrGetRoom(
                request.getUserId(),
                request.getAdminId()
        );

        return ResponseEntity.ok(
                new ResponseDto<>(200, "Room created/fetched successfully", roomId)
        );
    }

    //  ACCEPT CHAT
    @PostMapping("/accept")
    public ResponseEntity<ResponseDto<String>> accept(
            @Valid @RequestBody RoomRequestDTO request) {

        if (request.getRoomId() == null || request.getRoomId().trim().isEmpty()) {
            throw new BadRequestException("RoomId is required");
        }

        chatService.acceptChat(request.getRoomId());

        return ResponseEntity.ok(
                new ResponseDto<>(200, "Chat accepted. Conversation started", request.getRoomId())
        );
    }

    //  REJECT CHAT
    @PostMapping("/reject")
    public ResponseEntity<ResponseDto<String>> reject(
            @Valid @RequestBody RoomRequestDTO request) {

        if (request.getRoomId() == null || request.getRoomId().trim().isEmpty()) {
            throw new BadRequestException("RoomId is required");
        }

        chatService.rejectChat(request.getRoomId());

        return ResponseEntity.ok(
                new ResponseDto<>(200, "Chat rejected", request.getRoomId())
        );
    }
}