package com.caryanam.caryanam_broker.socket;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long adminId;

    @Column(unique = true)
    private String roomId; // userId_adminId


    @Column(nullable = false)
    private boolean firstMessageSent = false;
}