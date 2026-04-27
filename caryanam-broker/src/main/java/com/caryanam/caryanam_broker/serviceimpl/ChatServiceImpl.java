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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatRoomRepository chatRoomRepo;
    @Autowired
    private MessageRepository messageRepo;
    @Autowired
    private SocketIOServer socketServer;
    @Autowired
    private UserStatusRepository statusRepo;


    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private String generateRoomId(Long userId, Long ownerId) {
        return (userId < ownerId)
                ? userId + "_" + ownerId
                : ownerId + "_" + userId;
    }

    @Override
    public String createOrGetRoom(Long userId, Long ownerId) {

        Long uId = Math.min(userId, ownerId);
        Long oId = Math.max(userId, ownerId);

        String roomId = generateRoomId(uId, oId);

        return chatRoomRepo.findByUserIdAndOwnerId(uId, oId)
                .map(ChatRoom::getRoomId)
                .orElseGet(() -> {
                    try {
                        ChatRoom room = new ChatRoom();
                        room.setUserId(uId);
                        room.setOwnerId(oId);
                        room.setRoomId(roomId);
                        room.setFirstMessageSent(false);

                        chatRoomRepo.save(room);
                        return roomId;

                    } catch (Exception e) {

                        return chatRoomRepo.findByUserIdAndOwnerId(uId, oId)
                                .map(ChatRoom::getRoomId)
                                .orElse(roomId);
                    }
                });
    }



    @Override
    public MessageResponseDTO sendMessage(MessageRequestDTO dto) {

        if (dto == null || dto.getSenderId() == null || dto.getReceiverId() == null) {throw new BadRequestException("Invalid request");}
        if (!"USER".equals(dto.getSenderRole()) && !"PROPERTY_OWNER".equals(dto.getSenderRole())) {throw new BadRequestException("Invalid sender role");}

        Long userId;
        Long ownerId;

        if ("USER".equals(dto.getSenderRole())) {
            userId = dto.getSenderId();
            ownerId = dto.getReceiverId();
        } else {
            ownerId = dto.getSenderId();
            userId = dto.getReceiverId();
        }

        String roomId = createOrGetRoom(userId, ownerId);
        ChatRoom room = chatRoomRepo.findByRoomId(roomId).orElseThrow(() -> new RuntimeException("Room not found"));


        if (!room.isFirstMessageSent()) {

            if (!"USER".equals(dto.getSenderRole())) {throw new BadRequestException("First message must be sent by USER");}

            Message firstMsg = new Message();
            firstMsg.setRoomId(roomId);
            firstMsg.setSenderId(userId);
            firstMsg.setSenderRole("USER");
            firstMsg.setContent("Hi, I’m interested in your property. Could you please share more information?");
            firstMsg.setTimestamp(LocalDateTime.now());
            firstMsg.setRead(false);
            firstMsg.setStatus(MessageStatus.PENDING);

            messageRepo.save(firstMsg);
            room.setFirstMessageSent(true);
            chatRoomRepo.save(room);
            MessageResponseDTO response = mapToDTO(firstMsg);
            socketServer.getRoomOperations(roomId).sendEvent("receive_message", response);
            return response;
        }

        if (!room.isAccepted() && room.isFirstMessageSent() && "USER".equals(dto.getSenderRole())) {
            throw new BadRequestException("Please wait until owner accepts the chat");
        }
        if (!room.isAccepted() && "PROPERTY_OWNER".equals(dto.getSenderRole())) {
            throw new BadRequestException("Owner cannot send message until chat is accepted");
        }
        if (room.isRejected()) {
            throw new BadRequestException("Chat is rejected. You cannot send messages.");
        }

        Message msg = new Message();
        msg.setRoomId(roomId);
        msg.setSenderId(dto.getSenderId());
        msg.setSenderRole(dto.getSenderRole());
        msg.setContent(dto.getMessage());
        msg.setTimestamp(LocalDateTime.now());
        msg.setRead(false);
        msg.setStatus(room.isAccepted() ? MessageStatus.ACCEPTED : MessageStatus.PENDING);

        messageRepo.save(msg);
        MessageResponseDTO response = mapToDTO(msg);
        socketServer.getRoomOperations(roomId).sendEvent("receive_message", response);
        return response;
    }

    @Override
    public void acceptChat(String roomId) {

        ChatRoom room = chatRoomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setAccepted(true);
        chatRoomRepo.save(room);


        Message autoReply = new Message();
        autoReply.setRoomId(roomId);
        autoReply.setSenderId(room.getOwnerId());
        autoReply.setSenderRole("PROPERTY_OWNER");
        autoReply.setContent("Please wait, someone will connect with you shortly.");
        autoReply.setTimestamp(LocalDateTime.now());
        autoReply.setRead(false);
        autoReply.setStatus(MessageStatus.ACCEPTED);

        messageRepo.save(autoReply);
        MessageResponseDTO response = mapToDTO(autoReply);
        socketServer.getRoomOperations(roomId)
                .sendEvent("chat_accepted", roomId);
        socketServer.getRoomOperations(roomId)
                .sendEvent("receive_message", response);
    }


    @Override
    public void rejectChat(String roomId) {

        ChatRoom room = chatRoomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setAccepted(false);
        room.setRejected(true);
        chatRoomRepo.save(room);

        Message rejectMsg = new Message();
        rejectMsg.setRoomId(roomId);
        rejectMsg.setSenderId(room.getOwnerId());
        rejectMsg.setSenderRole("SYSTEM");
        rejectMsg.setContent("Chat has been rejected by the owner.");
        rejectMsg.setTimestamp(LocalDateTime.now());
        rejectMsg.setStatus(MessageStatus.REJECTED);

        messageRepo.save(rejectMsg);
        socketServer.getRoomOperations(roomId)
                .sendEvent("chat_rejected", mapToDTO(rejectMsg));
    }



    @Override
    public void handleTyping(TypingDTO dto) {
        if (dto.getRoomId() == null) {return;}
        socketServer.getRoomOperations(dto.getRoomId()).sendEvent("typing", dto);
    }

    @Override
    public void updateUserStatus(Long userId, boolean online) {
        if (userId == null) return;
        UserStatus status = statusRepo.findById(userId)
                .orElseGet(() -> {UserStatus s = new UserStatus();
                    s.setUserId(userId);
                    return s;});

        status.setOnline(online);
        statusRepo.save(status);
        socketServer.getBroadcastOperations().sendEvent("user_status", status);
    }

    @Override
    public MessageResponseDTO mapToDTO(Message msg) {

        String time = null;
        if (msg.getTimestamp() != null) {
            time = msg.getTimestamp().format(FORMATTER);}

        return new MessageResponseDTO(
                msg.getRoomId(),
                msg.getSenderId(),
                msg.getSenderRole(),
                msg.getContent(),
                time);
    }

    @Override
    public List<PendingChatDTO> getPendingChats(Long ownerId) {

        List<ChatRoom> rooms = chatRoomRepo.findByOwnerIdAndFirstMessageSentTrueAndAcceptedFalseAndIsRejectedFalse(ownerId);
        List<PendingChatDTO> response = new ArrayList<>();

        for (ChatRoom room : rooms) {

            List<Message> messages = messageRepo.findByRoomId(room.getRoomId());
            String lastMessage = "";
            String time = "";

            if (!messages.isEmpty()) {
                Message lastMsg = messages.get(messages.size() - 1);
                lastMessage = lastMsg.getContent();
                if (lastMsg.getTimestamp() != null) {
                    time = lastMsg.getTimestamp().toString();
                }
            }
            response.add(new PendingChatDTO(
                    room.getRoomId(),
                    room.getUserId(),
                    room.getOwnerId(),
                    lastMessage,
                    time));
        }

        return response;
    }

    @Override
    public List<AcceptedChatDTO> getAcceptedChats(Long ownerId) {

        List<ChatRoom> rooms = chatRoomRepo.findByOwnerIdAndAcceptedTrue(ownerId);
        List<AcceptedChatDTO> response = new ArrayList<>();

        for (ChatRoom room : rooms) {
            Message lastMsg = messageRepo.findTopByRoomIdOrderByTimestampDesc(room.getRoomId());
            String lastMessage = "";
            String time = "";

            if (lastMsg != null) {
                lastMessage = lastMsg.getContent();
                if (lastMsg.getTimestamp() != null) {
                    time = lastMsg.getTimestamp().toString();
                }
            }

            response.add(new AcceptedChatDTO(
                    room.getRoomId(),
                    room.getUserId(),
                    room.getOwnerId(),
                    lastMessage,
                    time));
        }

        return response;
    }

    @Override
    public List<PendingChatDTO> getRejectedChats(Long ownerId) {

        List<ChatRoom> rooms = chatRoomRepo.findByOwnerIdAndIsRejectedTrue(ownerId);
        List<PendingChatDTO> response = new ArrayList<>();

        for (ChatRoom room : rooms) {
            Message lastMsg = messageRepo.findTopByRoomIdOrderByTimestampDesc(room.getRoomId());

            String lastMessage = "";
            String time = "";
            if (lastMsg != null) {
                lastMessage = lastMsg.getContent();
                if (lastMsg.getTimestamp() != null) {
                    time = lastMsg.getTimestamp().toString();
                }
            }

            response.add(new PendingChatDTO(
                    room.getRoomId(),
                    room.getUserId(),
                    room.getOwnerId(),
                    lastMessage,
                    time));
        }
        return response;
    }


}