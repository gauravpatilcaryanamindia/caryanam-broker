package com.caryanam.caryanam_broker.serviceimpl;


import com.caryanam.caryanam_broker.entity.*;
import com.caryanam.caryanam_broker.enums.MessageStatus;
import com.caryanam.caryanam_broker.exception.BadRequestException;
import com.caryanam.caryanam_broker.repository.*;
import com.caryanam.caryanam_broker.service.ChatService;
import com.caryanam.caryanam_broker.socket.*;

import jdk.jshell.Snippet;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOServer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    @Autowired
    private  ChatRoomRepository chatRoomRepo;
    @Autowired
    private  MessageRepository messageRepo;
    @Autowired
    private  SocketIOServer socketServer;
    @Autowired
    private  UserStatusRepository  statusRepo;


    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ================= ROOM =================
    private String generateRoomId(Long userId, Long adminId) {
        return (userId < adminId)
                ? userId + "_" + adminId
                : adminId + "_" + userId;
    }

    @Override
    public String createOrGetRoom(Long userId, Long adminId) {

        Long uId = Math.min(userId, adminId);
        Long aId = Math.max(userId, adminId);

        String roomId = generateRoomId(uId, aId);

        return chatRoomRepo.findByUserIdAndAdminId(uId, aId)
                .map(ChatRoom::getRoomId)
                .orElseGet(() -> {
                    try {
                        ChatRoom room = new ChatRoom();
                        room.setUserId(uId);
                        room.setAdminId(aId);
                        room.setRoomId(roomId);
                        room.setFirstMessageSent(false);

                        chatRoomRepo.save(room);
                        return roomId;

                    } catch (Exception e) {

                        return chatRoomRepo.findByUserIdAndAdminId(uId, aId)
                                .map(ChatRoom::getRoomId)
                                .orElse(roomId);
                    }
                });
    }

    // ================= SEND MESSAGE =================
    @Override
    public MessageResponseDTO sendMessage(MessageRequestDTO dto) {

        Long userId;
        Long adminId;

        if ("USER".equals(dto.getSenderRole())) {
            userId = dto.getSenderId();
            adminId = dto.getReceiverId();
        } else {
            adminId = dto.getSenderId();
            userId = dto.getReceiverId();
        }

        String roomId = createOrGetRoom(userId, adminId);
        ChatRoom room = chatRoomRepo.findByUserIdAndAdminId(
                Math.min(userId, adminId),
                Math.max(userId, adminId)
        ).orElseThrow(() -> new RuntimeException("Room not found"));


        List<Message> existingMessages = messageRepo.findByRoomId(roomId);
        if (existingMessages.isEmpty() && !room.isFirstMessageSent()) {
            Message firstMsg = new Message();
            firstMsg.setRoomId(roomId);
            firstMsg.setSenderId(userId);
            firstMsg.setSenderRole("USER");
            firstMsg.setContent("Hi, I’m interested in your property. Could you please share more information?");
            firstMsg.setTimestamp(LocalDateTime.now());
            firstMsg.setRead(false);
            firstMsg.setStatus(MessageStatus.PENDING);

            messageRepo.save(firstMsg);

            Message adminReply = new Message();
            adminReply.setRoomId(roomId);
            adminReply.setSenderId(adminId);
            adminReply.setSenderRole("ADMIN");
            adminReply.setContent("Please wait, someone will connect with you shortly.");
            adminReply.setTimestamp(LocalDateTime.now());
            adminReply.setRead(false);
            adminReply.setStatus(MessageStatus.PENDING);

            messageRepo.save(adminReply);
            room.setFirstMessageSent(true);
            chatRoomRepo.save(room);

            if (existingMessages.isEmpty()) {
                socketServer.getRoomOperations(roomId).sendEvent("receive_message", mapToDTO(firstMsg));
                socketServer.getRoomOperations(roomId).sendEvent("receive_message", mapToDTO(adminReply));}
            return mapToDTO(firstMsg);
        }

        // ✅ ADD HERE
         if (room.isFirstMessageSent() && !isChatAccepted(roomId)) {
            throw new BadRequestException("Chat not accepted yet");
        }


        Message msg = new Message();
        msg.setRoomId(roomId);
        msg.setSenderId(dto.getSenderId());
        msg.setSenderRole(dto.getSenderRole());
        msg.setContent(dto.getMessage());
        msg.setTimestamp(LocalDateTime.now());
        msg.setRead(false);
        msg.setStatus(MessageStatus.ACCEPTED);

        messageRepo.save(msg);
        MessageResponseDTO response = mapToDTO(msg);
        socketServer.getRoomOperations(roomId).sendEvent("receive_message", response);
        return response;
    }

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

    // ================= REJECT =================
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

    @Override
    public boolean isChatAccepted(String roomId) {
        return messageRepo.existsByRoomIdAndStatus(roomId, MessageStatus.ACCEPTED);
    }

    @Override
    public void handleTyping(TypingDTO dto) {

        if (dto.getRoomId() == null) {
            return;
        }
        socketServer.getRoomOperations(dto.getRoomId())
                .sendEvent("typing", dto);
    }



    @Override
    public void updateUserStatus(Long userId, boolean online) {
        if (userId == null) return;

        UserStatus status = statusRepo.findById(userId)
                .orElseGet(() -> {
                    UserStatus s = new UserStatus();
                    s.setUserId(userId);
                    return s;});

        status.setOnline(online);
        statusRepo.save(status);
        socketServer.getBroadcastOperations()
                .sendEvent("user_status", status);
    }

    @Override
    public MessageResponseDTO mapToDTO(Message msg) {

        String time = null;

        if (msg.getTimestamp() != null) {
            time = msg.getTimestamp().format(FORMATTER);
        }

        return new MessageResponseDTO(
                msg.getRoomId(),
                msg.getSenderId(),
                msg.getSenderRole(),
                msg.getContent(),
                time
        );
    }


}