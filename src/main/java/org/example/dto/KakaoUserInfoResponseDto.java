package org.example.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 카카오 사용자 정보를 담는 데이터 전송 객체 (DTO)
 * 
 * 카카오 사용자 정보 API 응답에서 필요한 정보만 추출하여 담는 클래스
 * 
 * 카카오 API 응답 구조:
 * {
 *   "id": 123456789,
 *   "kakao_account": {
 *     "profile": {
 *       "nickname": "사용자닉네임"
 *     }
 *   }
 * }
 * 
 * @author Ssak3 Backend Team
 * @version 1.0
 * @since 2024-10-05
 */
@Data
@Builder
public class KakaoUserInfoResponseDto {
    
    /**
     * 카카오 사용자 고유 ID
     * 카카오에서 제공하는 사용자 식별자
     * Long 타입으로 저장 (카카오 ID는 숫자 형태)
     */
    private Long id;
    
    /**
     * 카카오 사용자 닉네임
     * 카카오 프로필에서 설정한 닉네임
     * String 타입으로 저장
     */
    private String nickname;
}
