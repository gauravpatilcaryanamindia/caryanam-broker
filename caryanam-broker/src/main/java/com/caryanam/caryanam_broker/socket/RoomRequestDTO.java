package com.caryanam.caryanam_broker.socket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequestDTO {

    private String roomId;
    private Long userId;
    private Long adminId;

}