package com.caryanam.caryanam_broker.service;


import com.caryanam.caryanam_broker.socket.MessageRequestDTO;
import com.caryanam.caryanam_broker.socket.MessageResponseDTO;
import com.caryanam.caryanam_broker.socket.TypingDTO;

public interface ChatService {

    MessageResponseDTO sendMessage(MessageRequestDTO dto);

    String createOrGetRoom(Long userId, Long adminId);

    void handleTyping(TypingDTO dto);

    void updateUserStatus(Long userId, boolean online);

    void acceptChat(String roomId);

    void rejectChat(String roomId);

    boolean isChatAccepted(String roomId);
}