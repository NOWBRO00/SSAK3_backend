package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.ChatRoom;
import org.example.entity.Message;
import org.example.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping({"/api/chat", "/api/chatrooms"})  // 프론트엔드 호환성을 위한 경로 추가
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 채팅방 생성 또는 조회 (프론트엔드 호환성을 위해 두 경로 모두 지원)
    @PostMapping({"/rooms", ""})  // /api/chat/rooms, /api/chatrooms/rooms, /api/chatrooms 모두 지원
    public ResponseEntity<ChatRoom> getOrCreateChatRoom(
            @RequestParam Long buyerId,
            @RequestParam Long sellerId,
            @RequestParam Long productId
    ) {
        try {
            log.info("채팅방 생성/조회 요청: buyerId={}, sellerId={}, productId={}", buyerId, sellerId, productId);
            ChatRoom chatRoom = chatService.getOrCreateChatRoom(buyerId, sellerId, productId);
            log.info("채팅방 조회/생성 성공: chatRoomId={}, buyerId={}, sellerId={}, productId={}", 
                    chatRoom.getId(), buyerId, sellerId, productId);
            return ResponseEntity.ok(chatRoom);
        } catch (IllegalArgumentException e) {
            log.error("채팅방 조회/생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("채팅방 조회/생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 채팅방 상세 조회 (프론트엔드 호환성을 위해 두 경로 모두 지원)
    @GetMapping({"/rooms/{chatRoomId}", "/{chatRoomId}"})
    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable Long chatRoomId) {
        try {
            log.info("GET /api/chat/rooms/{} 요청 받음", chatRoomId);
            ChatRoom chatRoom = chatService.getChatRoomById(chatRoomId);
            if (chatRoom != null) {
                log.info("채팅방 조회 성공: chatRoomId={}", chatRoomId);
                return ResponseEntity.ok(chatRoom);
            } else {
                log.warn("채팅방을 찾을 수 없습니다: chatRoomId={}", chatRoomId);
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            log.warn("채팅방 조회 실패: chatRoomId={}, error={}", chatRoomId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("채팅방 조회 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 사용자의 채팅방 목록 조회
    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<List<ChatRoom>> getUserChatRooms(@PathVariable Long userId) {
        try {
            log.info("GET /api/chat/rooms/user/{} 요청 받음", userId);
            List<ChatRoom> chatRooms = chatService.getUserChatRooms(userId);
            log.info("채팅방 목록 조회 성공: userId={}, count={}", userId, chatRooms != null ? chatRooms.size() : 0);
            return ResponseEntity.ok(chatRooms != null ? chatRooms : new ArrayList<>());
        } catch (IllegalArgumentException e) {
            log.warn("채팅방 목록 조회 실패 (사용자 없음): userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        } catch (Exception e) {
            log.error("채팅방 목록 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            e.printStackTrace();
            // 예외가 발생해도 빈 리스트를 반환하여 500 에러 방지
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    

    // 채팅방의 메시지 목록 조회
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<List<Message>> getChatRoomMessages(@PathVariable Long chatRoomId) {
        try {
            List<Message> messages = chatService.getChatRoomMessages(chatRoomId);
            log.info("메시지 목록 조회 성공: chatRoomId={}, count={}", chatRoomId, messages != null ? messages.size() : 0);
            return ResponseEntity.ok(messages != null ? messages : new ArrayList<>());
        } catch (IllegalArgumentException e) {
            log.error("메시지 목록 조회 실패: chatRoomId={}, error={}", chatRoomId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("메시지 목록 조회 중 오류 발생: chatRoomId={}", chatRoomId, e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 메시지 전송 (RequestBody 또는 쿼리 파라미터 모두 지원)
    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long chatRoomId,
            @RequestParam Long senderId,
            @RequestParam(required = false) String content,
            @RequestBody(required = false) Map<String, String> requestBody
    ) {
        try {
            // content를 RequestBody 또는 쿼리 파라미터에서 가져오기
            String messageContent = content;
            if (messageContent == null && requestBody != null) {
                messageContent = requestBody.get("content");
            }
            
            if (messageContent == null || messageContent.trim().isEmpty()) {
                log.error("메시지 전송 실패: content가 비어있습니다. chatRoomId={}, senderId={}", chatRoomId, senderId);
                return ResponseEntity.badRequest().build();
            }
            
            log.info("메시지 전송 요청: chatRoomId={}, senderId={}, content={}", chatRoomId, senderId, messageContent);
            Message message = chatService.sendMessage(chatRoomId, senderId, messageContent);
            log.info("메시지 전송 성공: messageId={}, chatRoomId={}, senderId={}", 
                    message.getId(), chatRoomId, senderId);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            log.error("메시지 전송 실패: chatRoomId={}, senderId={}, error={}", 
                    chatRoomId, senderId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생: chatRoomId={}, senderId={}", chatRoomId, senderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 메시지 읽음 처리
    @PutMapping("/rooms/{chatRoomId}/read")
    public ResponseEntity<String> markMessagesAsRead(
            @PathVariable Long chatRoomId,
            @RequestParam Long userId
    ) {
        try {
            chatService.markMessagesAsRead(chatRoomId, userId);
            log.info("메시지 읽음 처리 성공: chatRoomId={}, userId={}", chatRoomId, userId);
            return ResponseEntity.ok("메시지가 읽음 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("메시지 읽음 처리 실패: chatRoomId={}, userId={}, error={}", 
                    chatRoomId, userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("메시지 읽음 처리 중 오류 발생: chatRoomId={}, userId={}", chatRoomId, userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 채팅방 삭제
    @DeleteMapping("/rooms/{chatRoomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long chatRoomId) {
        try {
            chatService.deleteChatRoom(chatRoomId);
            log.info("채팅방 삭제 성공: chatRoomId={}", chatRoomId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("채팅방 삭제 중 오류 발생: chatRoomId={}", chatRoomId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}



