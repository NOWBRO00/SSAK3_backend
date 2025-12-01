package org.example.repository;

import org.example.entity.ChatRoom;
import org.example.entity.Message;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // 채팅방의 모든 메시지 조회 (시간순 정렬)
    @EntityGraph(attributePaths = {"sender", "chatRoom"})
    List<Message> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    // 채팅방의 읽지 않은 메시지 수 조회
    long countByChatRoomAndIsReadFalse(ChatRoom chatRoom);

    // 특정 사용자가 읽지 않은 메시지 수 조회
    long countByChatRoomAndSenderNotAndIsReadFalse(ChatRoom chatRoom, org.example.entity.UserProfile sender);
}



