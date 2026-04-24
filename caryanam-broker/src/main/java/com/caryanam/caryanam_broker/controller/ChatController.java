package com.caryanam.caryanam_broker.controller;

import com.caryanam.caryanam_broker.dto.ResponseDto;
import com.caryanam.caryanam_broker.exception.BadRequestException;
import com.caryanam.caryanam_broker.exception.InvalidOperationException;
import com.caryanam.caryanam_broker.repository.AdminRepository;
import com.caryanam.caryanam_broker.repository.MessageRepository;
import com.caryanam.caryanam_broker.repository.UserRepository;
import com.caryanam.caryanam_broker.service.ChatService;
import com.caryanam.caryanam_broker.socket.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    @Autowired
    private  ChatService chatService;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  AdminRepository adminRepository;
    @Autowired
    private MessageRepository messageRepo;


    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequestDTO dto) {

        if (dto == null) {throw new InvalidOperationException("Request body is missing");}
        if (dto.getSenderId() == null || dto.getReceiverId() == null) {
            throw new InvalidOperationException("SenderId and ReceiverId are required");
        }
        if (!"USER".equals(dto.getSenderRole())) {
            throw new InvalidOperationException("Only USER can send message to ADMIN");
        }
        Long userId = dto.getSenderId();
        Long adminId = dto.getReceiverId();
        if (!userRepository.existsById(userId)) {
            throw new InvalidOperationException("User not found with id: " + userId);}
        if (!adminRepository.existsById(adminId)) {
            throw new InvalidOperationException("Admin not found with id: " + adminId);}

        MessageResponseDTO response = chatService.sendMessage(dto);
        return ResponseEntity.ok(
                new ResponseDto<>(
                        200, "Message processed successfully", response));
    }

    @PostMapping("/room")
    public ResponseEntity<ResponseDto<String>> createRoom(
            @Valid @RequestBody RoomRequestDTO request) {

         String roomId = chatService.createOrGetRoom(
                request.getUserId(),
                request.getAdminId());

        return ResponseEntity.ok(new ResponseDto<>(200, "Room created/fetched successfully", roomId));
    }


    @PostMapping("/accept")
    public ResponseEntity<ResponseDto<String>> accept(
            @Valid @RequestBody RoomRequestDTO request) {

        if (request.getRoomId() == null || request.getRoomId().trim().isEmpty()) {
            throw new BadRequestException("RoomId is required");
        }
        chatService.acceptChat(request.getRoomId());
        return ResponseEntity.ok(new ResponseDto<>(200, "Chat accepted. Conversation started", request.getRoomId()));
    }


    @PostMapping("/reject")
    public ResponseEntity<ResponseDto<String>> reject(
            @Valid @RequestBody RoomRequestDTO request) {

        if (request.getRoomId() == null || request.getRoomId().trim().isEmpty()) {
            throw new BadRequestException("RoomId is required");
        }
        chatService.rejectChat(request.getRoomId());
        return ResponseEntity.ok(new ResponseDto<>(200, "Chat rejected", request.getRoomId()));
    }

    @PostMapping("/typing")
    public ResponseEntity<ResponseDto<String>> typing(
            @RequestBody TypingDTO dto) {

        if (dto.getRoomId() == null) {
            throw new BadRequestException("RoomId is required");
        }
        chatService.handleTyping(dto);
        return ResponseEntity.ok(new ResponseDto<>(200, "Typing event sent", dto.getRoomId()));
    }

    @PostMapping("/status")
    public ResponseEntity<ResponseDto<String>> updateStatus(
            @RequestParam Long userId,
            @RequestParam boolean online) {

        if (userId == null) {throw new BadRequestException("UserId is required");}

        chatService.updateUserStatus(userId, online);
        return ResponseEntity.ok(new ResponseDto<>(200, "User status updated", userId.toString()));
    }
    @GetMapping("/history/{roomId}")
    public ResponseEntity<ResponseDto<List<MessageResponseDTO>>> getChatHistory(
            @PathVariable String roomId) {

        if (roomId == null || roomId.trim().isEmpty()) {
            throw new BadRequestException("RoomId is required");
        }

        List<Message> messages = messageRepo.findByRoomId(roomId);
        List<MessageResponseDTO> responseList = new ArrayList<>();

        for (Message msg : messages) {
            responseList.add(chatService.mapToDTO(msg));
        }

        return ResponseEntity.ok(
                new ResponseDto<>(
                        200,
                        "Chat history fetched successfully",
                        responseList
                )
        );
    }

}