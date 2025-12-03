package org.example.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "messages")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "chatRoom", "sender"}, ignoreUnknown = true)
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    @JsonIgnore
    private ChatRoom chatRoom;

    // 발신자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnore
    private UserProfile sender;

    // 메시지 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 읽음 여부
    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    // 프론트엔드가 기대하는 형식에 맞추기 위한 getter 메서드들
    @JsonGetter("chatRoomId")
    public Long getChatRoomId() {
        return chatRoom != null ? chatRoom.getId() : null;
    }

    @JsonGetter("senderId")
    public Long getSenderId() {
        return sender != null ? sender.getId() : null;
    }

    @JsonGetter("sender")
    public UserProfile getSenderForJson() {
        return sender;
    }

    // 순환 참조 방지를 위해 chatRoom은 반환하지 않음
    // chatRoomId만 제공하면 충분함
    // @JsonGetter("chatRoom")
    // public ChatRoom getChatRoomForJson() {
    //     return chatRoom;
    // }
}

