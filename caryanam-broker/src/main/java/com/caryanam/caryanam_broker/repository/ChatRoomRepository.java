package com.caryanam.caryanam_broker.repository;


import com.caryanam.caryanam_broker.socket.ChatRoom;
import com.caryanam.caryanam_broker.socket.Message;
import com.caryanam.caryanam_broker.socket.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByUserIdAndOwnerId(Long userId, Long ownerId);
    Optional<ChatRoom> findByRoomId(String roomId);

    List<ChatRoom> findByOwnerIdAndFirstMessageSentTrueAndAcceptedFalseAndIsRejectedFalse(Long ownerId);
    List<ChatRoom> findByOwnerIdAndAcceptedTrue(Long ownerId);
    List<ChatRoom> findByOwnerIdAndIsRejectedTrue(Long ownerId);
}

