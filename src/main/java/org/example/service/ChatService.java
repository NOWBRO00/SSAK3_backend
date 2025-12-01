package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.ChatRoom;
import org.example.entity.Message;
import org.example.entity.Product;
import org.example.entity.UserProfile;
import org.example.repository.ChatRoomRepository;
import org.example.repository.MessageRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProductRepository productRepository;

    // 채팅방 생성 또는 조회 (이미 있으면 반환)
    public ChatRoom getOrCreateChatRoom(Long buyerId, Long sellerId, Long productId) {
        // 사용자 및 상품 조회
        UserProfile buyer = userProfileRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다."));
        
        UserProfile seller = userProfileRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 이미 존재하는 채팅방이 있는지 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByBuyerAndSellerAndProduct(buyer, seller, product);
        
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    // 메시지 전송
    public Message sendMessage(Long chatRoomId, Long senderId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        
        UserProfile sender = userProfileRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));

        // 발신자가 채팅방의 구매자 또는 판매자인지 확인
        if (!chatRoom.getBuyer().getId().equals(senderId) && 
            !chatRoom.getSeller().getId().equals(senderId)) {
            throw new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다.");
        }

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .isRead(false)
                .build();

        return messageRepository.save(message);
    }

    // 채팅방 목록 조회 (사용자가 참여한 모든 채팅방)
    public List<ChatRoom> getUserChatRooms(Long userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElse(null);
        
        if (user == null) {
            // 카카오 ID로 시도
            user = userProfileRepository.findByKakaoId(userId);
        }
        
        if (user == null) {
            return new ArrayList<>();
        }

        List<ChatRoom> chatRooms = chatRoomRepository.findByBuyerOrSeller(user, user);
        
        // Lazy 로딩 엔티티 초기화
        if (chatRooms != null && !chatRooms.isEmpty()) {
            chatRooms.forEach(room -> {
                try {
                    if (room.getBuyer() != null) {
                        room.getBuyer().getId();
                        room.getBuyer().getNickname();
                    }
                    if (room.getSeller() != null) {
                        room.getSeller().getId();
                        room.getSeller().getNickname();
                    }
                    if (room.getProduct() != null) {
                        room.getProduct().getId();
                        room.getProduct().getTitle();
                    }
                    // 메시지 초기화
                    if (room.getMessages() != null) {
                        room.getMessages().size();
                    }
                } catch (Exception e) {
                    log.error("채팅방 초기화 중 오류: {}", e.getMessage());
                }
            });
        }
        
        return chatRooms != null ? chatRooms : new ArrayList<>();
    }

    // 채팅방의 메시지 목록 조회
    public List<Message> getChatRoomMessages(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        List<Message> messages = messageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
        
        // Lazy 로딩 엔티티 초기화
        if (messages != null && !messages.isEmpty()) {
            messages.forEach(message -> {
                try {
                    if (message.getSender() != null) {
                        message.getSender().getId();
                        message.getSender().getNickname();
                    }
                    if (message.getChatRoom() != null) {
                        message.getChatRoom().getId();
                    }
                } catch (Exception e) {
                    log.error("메시지 초기화 중 오류: {}", e.getMessage());
                }
            });
        }
        
        return messages != null ? messages : new ArrayList<>();
    }

    // 메시지 읽음 처리
    public void markMessagesAsRead(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        
        // 사용자 존재 확인
        UserProfile user = userProfileRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            user = userProfileRepository.findByKakaoId(userId);
        }
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 상대방이 보낸 메시지만 읽음 처리 (더 효율적으로)
        final Long finalUserId = user.getId();
        List<Message> messages = messageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
        messages.stream()
                .filter(msg -> !msg.getSender().getId().equals(finalUserId) && !msg.isRead())
                .forEach(msg -> {
                    msg.setRead(true);
                    messageRepository.save(msg);
                });
    }

    // 채팅방 삭제
    public void deleteChatRoom(Long chatRoomId) {
        chatRoomRepository.deleteById(chatRoomId);
    }
}

