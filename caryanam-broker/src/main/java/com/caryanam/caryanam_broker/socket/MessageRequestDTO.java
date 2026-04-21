package com.caryanam.caryanam_broker.socket;

import lombok.Data;

@Data
public class MessageRequestDTO {

    private Long senderId;
    private Long receiverId;
    private String senderRole;
    private String message;
}
