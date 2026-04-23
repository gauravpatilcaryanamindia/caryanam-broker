package com.caryanam.caryanam_broker.repository;
import com.caryanam.caryanam_broker.socket.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByUserIdAndAdminId(Long userId, Long adminId);
}

