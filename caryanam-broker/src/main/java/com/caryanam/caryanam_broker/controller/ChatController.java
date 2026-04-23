package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.dto.ResponseDto;
import com.caryanam.caryanam_broker.exception.BadRequestException;
import com.caryanam.caryanam_broker.exception.InvalidOperationException;
import com.caryanam.caryanam_broker.repository.AdminRepository;
import com.caryanam.caryanam_broker.repository.UserRepository;
import com.caryanam.caryanam_broker.service.ChatService;
import com.caryanam.caryanam_broker.socket.MessageRequestDTO;
import com.caryanam.caryanam_broker.socket.MessageResponseDTO;
import com.caryanam.caryanam_broker.socket.RoomRequestDTO;

import com.caryanam.caryanam_broker.socket.TypingDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    // ================= SEND MESSAGE =================
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequestDTO dto) {

        if (dto == null) {
            throw new InvalidOperationException("Request body is missing");
        }

        if (dto.getSenderId() == null || dto.getReceiverId() == null) {
            throw new InvalidOperationException("SenderId and ReceiverId are required");
        }

        if (!"USER".equals(dto.getSenderRole())) {
            throw new InvalidOperationException("Only USER can send message to ADMIN");
        }

        if (dto.getSenderId().equals(dto.getReceiverId())) {
            throw new InvalidOperationException("Sender and Receiver cannot be same");
        }

        Long userId = dto.getSenderId();
        Long adminId = dto.getReceiverId();

        if (!userRepository.existsById(userId)) {
            throw new InvalidOperationException("User not found with id: " + userId);
        }

        if (!adminRepository.existsById(adminId)) {
            throw new InvalidOperationException("Admin not found with id: " + adminId);
        }

        MessageResponseDTO response = chatService.sendMessage(dto);

        return ResponseEntity.ok(
                new ResponseDto<>(
                        200,
                        "Message processed successfully",
                        response
                )
        );
    }
    // ================= CREATE ROOM =================
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

    // ================= ACCEPT CHAT =================
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

    // ================= REJECT CHAT =================
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

    @PostMapping("/typing")
    public ResponseEntity<ResponseDto<String>> typing(
            @RequestBody TypingDTO dto) {

        if (dto.getRoomId() == null) {
            throw new BadRequestException("RoomId is required");
        }

        chatService.handleTyping(dto);

        return ResponseEntity.ok(
                new ResponseDto<>(200, "Typing event sent", dto.getRoomId())
        );
    }
    @PostMapping("/status")
    public ResponseEntity<ResponseDto<String>> updateStatus(
            @RequestParam Long userId,
            @RequestParam boolean online) {

        if (userId == null) {
            throw new BadRequestException("UserId is required");
        }

        chatService.updateUserStatus(userId, online);

        return ResponseEntity.ok(
                new ResponseDto<>(200, "User status updated", userId.toString())
        );
    }
}