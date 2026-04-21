package com.caryanam.caryanam_broker.serviceimpl;


import com.caryanam.caryanam_broker.entity.*;
import com.caryanam.caryanam_broker.enums.MessageStatus;
import com.caryanam.caryanam_broker.exception.BadRequestException;
import com.caryanam.caryanam_broker.repository.*;
import com.caryanam.caryanam_broker.service.ChatService;
import com.caryanam.caryanam_broker.socket.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOServer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepo;
    private final MessageRepository messageRepo;
    private final UserStatusRepository statusRepo;
    private final SocketIOServer socketServer;

    private final UserRepository userRepo;
    private final AdminRepository adminRepo;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ================= ROOM ID =================
    private String generateRoomId(Long userId, Long adminId) {
        return (userId < adminId)
                ? userId + "_" + adminId
                : adminId + "_" + userId;
    }

    // ================= CREATE ROOM =================
    @Override
    public String createOrGetRoom(Long userId, Long adminId) {

        Long uId = Math.min(userId, adminId);
        Long aId = Math.max(userId, adminId);

        String roomId = generateRoomId(uId, aId);

        return chatRoomRepo.findByUserIdAndAdminId(uId, aId)
                .map(ChatRoom::getRoomId)
                .orElseGet(() -> {
                    ChatRoom room = new ChatRoom();
                    room.setUserId(uId);
                    room.setAdminId(aId);
                    room.setRoomId(roomId);
                    room.setFirstMessageSent(false);
                    chatRoomRepo.save(room);
                    return roomId;
                });
    }

    // ================= SEND MESSAGE =================
    @Override
    public MessageResponseDTO sendMessage(MessageRequestDTO dto) {

        Long userId;
        Long adminId;

        if (dto.getSenderId() == 1) {   // USER
            userId = dto.getSenderId();
            adminId = dto.getReceiverId();
        } else {                        // ADMIN
            adminId = dto.getSenderId();
            userId = dto.getReceiverId();
        }

        String roomId = createOrGetRoom(userId, adminId);

        // ================= CHECK FIRST MESSAGE =================
        ChatRoom room = chatRoomRepo.findByUserIdAndAdminId(
                Math.min(userId, adminId),
                Math.max(userId, adminId)
        ).orElseThrow(() -> new RuntimeException("Room not found"));

        // ================= AUTO FIRST MESSAGE =================
        if (!room.isFirstMessageSent()) {

            Message firstMsg = new Message();
            firstMsg.setRoomId(roomId);
            firstMsg.setSenderId(userId);
            firstMsg.setSenderRole("USER");
            firstMsg.setContent("Hi, I’m interested in your property. Could you please share more information?");
            firstMsg.setTimestamp(LocalDateTime.now());
            firstMsg.setRead(false);
            firstMsg.setStatus(MessageStatus.PENDING);

            messageRepo.save(firstMsg);

            // mark first message sent
            room.setFirstMessageSent(true);
            chatRoomRepo.save(room);

            // ================= AUTO ADMIN REPLY =================
            Message adminReply = new Message();
            adminReply.setRoomId(roomId);
            adminReply.setSenderId(adminId);
            adminReply.setSenderRole("ADMIN");
            adminReply.setContent("Please wait, an agent will connect with you shortly.");
            adminReply.setTimestamp(LocalDateTime.now());
            adminReply.setRead(false);
            adminReply.setStatus(MessageStatus.PENDING);

            messageRepo.save(adminReply);

            return new MessageResponseDTO(
                    roomId,
                    firstMsg.getSenderId(),
                    firstMsg.getSenderRole(),
                    firstMsg.getContent(),
                    firstMsg.getTimestamp().format(FORMATTER)
            );
        }

        // ================= NORMAL MESSAGE FLOW =================
        Message msg = new Message();
        msg.setRoomId(roomId);
        msg.setSenderId(dto.getSenderId());

        if (dto.getSenderId().equals(userId)) {
            msg.setSenderRole("USER");
        } else {
            msg.setSenderRole("ADMIN");
        }

        msg.setContent(dto.getMessage());
        msg.setTimestamp(LocalDateTime.now());
        msg.setRead(false);
        msg.setStatus(MessageStatus.PENDING);

        messageRepo.save(msg);

        return new MessageResponseDTO(
                roomId,
                msg.getSenderId(),
                msg.getSenderRole(),
                msg.getContent(),
                msg.getTimestamp().format(FORMATTER)
        );
    }



    // ================= ACCEPT CHAT =================
    @Override
    public void acceptChat(String roomId) {

        List<Message> messages = messageRepo.findByRoomId(roomId);

        for (Message msg : messages) {
            if (msg.getStatus() == MessageStatus.PENDING) {
                msg.setStatus(MessageStatus.ACCEPTED);
            }
        }

        messageRepo.saveAll(messages);

        socketServer.getRoomOperations(roomId)
                .sendEvent("chat_accepted", roomId);
    }

    // ================= REJECT CHAT =================
    @Override
    public void rejectChat(String roomId) {

        List<Message> messages = messageRepo.findByRoomId(roomId);

        for (Message msg : messages) {
            if (msg.getStatus() == MessageStatus.PENDING) {
                msg.setStatus(MessageStatus.REJECTED);
            }
        }

        messageRepo.saveAll(messages);

        socketServer.getRoomOperations(roomId)
                .sendEvent("chat_rejected", roomId);
    }

    // ================= TYPING =================
    @Override
    public void handleTyping(TypingDTO dto) {
        socketServer.getRoomOperations(dto.getRoomId())
                .sendEvent("typing", dto);
    }

    // ================= USER STATUS =================
    @Override
    public void updateUserStatus(Long userId, boolean online) {

        UserStatus status = new UserStatus();
        status.setUserId(userId);
        status.setOnline(online);

        statusRepo.save(status);

        socketServer.getBroadcastOperations()
                .sendEvent("user_status", status);
    }

    @Override
    public boolean isChatAccepted(String roomId) {
        return messageRepo.existsByRoomIdAndStatus(roomId, MessageStatus.ACCEPTED);
    }
}