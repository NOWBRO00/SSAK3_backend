# SSAK3 Backend API 명세서

## 기본 정보

- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **인증 방식**: 현재 미구현 (향후 JWT 토큰 예정)

---

## 1. 인증 (Authentication)

### 1.1 카카오 로그인

**엔드포인트**: `POST /api/auth/kakao`

**설명**: 카카오 인가 코드를 받아 로그인 처리 및 토큰 발급

**요청 헤더**:
```
Content-Type: application/json
```

**요청 본문**:
```json
{
  "code": "카카오_인가_코드"
}
```

**요청 예시**:
```json
{
  "code": "ToRe4dXtrwQKuB5CuqzeHvTWrnvsypQfjkU6HCG7NcgwOSIYYVjJngAAAAQKDQ0hAAABmroqIWotjdRiIM79qQ"
}
```

**성공 응답** (200 OK):
```json
{
  "accessToken": "access-550e8400-e29b-41d4-a716-446655440000",
  "refreshToken": "refresh-550e8400-e29b-41d4-a716-446655440000",
  "profile": {
    "id": 123456789,
    "nickname": "사용자닉네임",
    "email": "user@example.com",
    "profileImageUrl": "https://kakaocdn.net/...",
    "thumbnailImageUrl": "https://kakaocdn.net/..."
  }
}
```

**에러 응답**:

- **400 Bad Request** (검증 실패):
```json
{
  "code": "VALIDATION_ERROR",
  "message": "인가 코드는 필수입니다."
}
```

- **502 Bad Gateway** (카카오 API 오류):
```json
{
  "code": "KAKAO_API_ERROR",
  "message": "카카오 토큰 발급 요청이 실패했습니다. status=401 UNAUTHORIZED, body=null"
}
```

- **500 Internal Server Error**:
```json
{
  "code": "INTERNAL_ERROR",
  "message": "내부 서버 오류 메시지"
}
```

---

## 2. 헬스 체크

### 2.1 서버 상태 확인

**엔드포인트**: `GET /api/health`

**설명**: 서버가 정상적으로 실행 중인지 확인

**요청**: 헤더 없음

**성공 응답** (200 OK):
```json
{
  "status": "OK",
  "message": "Ssak3 Backend is running!"
}
```

---

## 3. 사용자 (Users)

### 3.1 사용자 등록

**엔드포인트**: `POST /api/users`

**설명**: 새 사용자 등록

**요청 본문**:
```json
{
  "kakaoId": 123456789,
  "nickname": "사용자닉네임",
  "profileImage": "https://...",
  "temperature": 36.5
}
```

**성공 응답** (200 OK):
```json
{
  "id": 1,
  "kakaoId": 123456789,
  "nickname": "사용자닉네임",
  "profileImage": "https://...",
  "temperature": 36.5,
  "createdAt": "2024-11-25T16:00:00",
  "updatedAt": "2024-11-25T16:00:00"
}
```

### 3.2 전체 사용자 조회

**엔드포인트**: `GET /api/users`

**설명**: 모든 사용자 목록 조회

**성공 응답** (200 OK):
```json
[
  {
    "id": 1,
    "kakaoId": 123456789,
    "nickname": "사용자1",
    "profileImage": "https://...",
    "temperature": 36.5,
    "createdAt": "2024-11-25T16:00:00",
    "updatedAt": "2024-11-25T16:00:00"
  },
  {
    "id": 2,
    "kakaoId": 987654321,
    "nickname": "사용자2",
    "profileImage": "https://...",
    "temperature": 36.5,
    "createdAt": "2024-11-25T16:00:00",
    "updatedAt": "2024-11-25T16:00:00"
  }
]
```

### 3.3 특정 사용자 조회

**엔드포인트**: `GET /api/users/{id}`

**설명**: ID로 특정 사용자 조회

**경로 변수**:
- `id` (Long): 사용자 ID

**성공 응답** (200 OK):
```json
{
  "id": 1,
  "kakaoId": 123456789,
  "nickname": "사용자닉네임",
  "profileImage": "https://...",
  "temperature": 36.5,
  "createdAt": "2024-11-25T16:00:00",
  "updatedAt": "2024-11-25T16:00:00"
}
```

**에러 응답** (404 Not Found):
- 사용자를 찾을 수 없을 때

### 3.4 사용자 삭제

**엔드포인트**: `DELETE /api/users/{id}`

**설명**: 특정 사용자 삭제

**경로 변수**:
- `id` (Long): 사용자 ID

**성공 응답** (200 OK): 본문 없음

---

## 4. 상품 (Products)

### 4.1 상품 등록 (이미지 포함)

**엔드포인트**: `POST /api/products/with-upload`

**설명**: 이미지와 함께 상품 등록

**요청 헤더**:
```
Content-Type: multipart/form-data
```

**요청 파라미터** (form-data):
- `title` (String, 필수): 상품 제목
- `price` (Integer, 필수): 상품 가격
- `description` (String, 선택): 상품 설명
- `categoryId` (Long, 필수): 카테고리 ID
- `sellerId` (Long, 필수): 판매자 ID
- `images` (MultipartFile[], 선택): 상품 이미지 파일들

**성공 응답** (200 OK):
```json
{
  "id": 1,
  "seller": { ... },
  "category": { ... },
  "title": "상품 제목",
  "description": "상품 설명",
  "price": 10000,
  "status": "ON_SALE",
  "images": [
    {
      "id": 1,
      "imageUrl": "/uploads/...",
      "orderIndex": 0
    }
  ],
  "createdAt": "2024-11-25T16:00:00",
  "updatedAt": "2024-11-25T16:00:00"
}
```

### 4.2 상품 등록 (JSON)

**엔드포인트**: `POST /api/products`

**설명**: 이미지 없이 상품 등록 (JSON)

**요청 본문**:
```json
{
  "seller": { "id": 1 },
  "category": { "id": 1 },
  "title": "상품 제목",
  "description": "상품 설명",
  "price": 10000,
  "status": "ON_SALE"
}
```

**성공 응답** (200 OK): 상품 객체

### 4.3 전체 상품 조회

**엔드포인트**: `GET /api/products`

**설명**: 모든 상품 목록 조회

**성공 응답** (200 OK):
```json
[
  {
    "id": 1,
    "title": "상품 제목",
    "description": "상품 설명",
    "price": 10000,
    "status": "ON_SALE",
    "images": [ ... ],
    "createdAt": "2024-11-25T16:00:00",
    "updatedAt": "2024-11-25T16:00:00"
  }
]
```

### 4.4 상품 상세 조회

**엔드포인트**: `GET /api/products/{id}`

**설명**: ID로 특정 상품 조회

**경로 변수**:
- `id` (Long): 상품 ID

**성공 응답** (200 OK): 상품 객체

**에러 응답** (404 Not Found): 상품을 찾을 수 없을 때

### 4.5 카테고리별 상품 조회

**엔드포인트**: `GET /api/products/category/{categoryId}`

**설명**: 특정 카테고리의 상품 목록 조회

**경로 변수**:
- `categoryId` (Long): 카테고리 ID

**성공 응답** (200 OK): 상품 목록 배열

### 4.6 판매자별 상품 조회

**엔드포인트**: `GET /api/products/seller/{sellerId}`

**설명**: 특정 판매자의 상품 목록 조회

**경로 변수**:
- `sellerId` (Long): 판매자 ID

**성공 응답** (200 OK): 상품 목록 배열

### 4.7 상품 수정

**엔드포인트**: `PUT /api/products/{id}`

**설명**: 상품 정보 수정

**경로 변수**:
- `id` (Long): 상품 ID

**요청 본문**:
```json
{
  "title": "수정된 제목",
  "description": "수정된 설명",
  "price": 15000,
  "status": "SOLD_OUT"
}
```

**성공 응답** (200 OK): 수정된 상품 객체

### 4.8 상품 삭제

**엔드포인트**: `DELETE /api/products/{id}`

**설명**: 상품 삭제

**경로 변수**:
- `id` (Long): 상품 ID

**성공 응답** (204 No Content): 본문 없음

---

## 5. 찜 (Likes)

### 5.1 찜 추가

**엔드포인트**: `POST /api/likes`

**설명**: 상품에 찜 추가

**요청 파라미터** (Query):
- `userId` (Long, 필수): 사용자 ID
- `productId` (Long, 필수): 상품 ID

**요청 예시**: `POST /api/likes?userId=1&productId=1`

**성공 응답** (200 OK):
```json
{
  "id": 1,
  "user": { "id": 1, ... },
  "product": { "id": 1, ... },
  "createdAt": "2024-11-25T16:00:00",
  "updatedAt": "2024-11-25T16:00:00"
}
```

### 5.2 찜 취소

**엔드포인트**: `DELETE /api/likes`

**설명**: 상품 찜 취소

**요청 파라미터** (Query):
- `userId` (Long, 필수): 사용자 ID
- `productId` (Long, 필수): 상품 ID

**요청 예시**: `DELETE /api/likes?userId=1&productId=1`

**성공 응답** (200 OK):
```json
"찜이 취소되었습니다."
```

### 5.3 사용자 찜 목록 조회

**엔드포인트**: `GET /api/likes/user/{userId}`

**설명**: 특정 사용자의 찜 목록 조회

**경로 변수**:
- `userId` (Long): 사용자 ID

**성공 응답** (200 OK):
```json
[
  {
    "id": 1,
    "user": { "id": 1, ... },
    "product": { "id": 1, ... },
    "createdAt": "2024-11-25T16:00:00",
    "updatedAt": "2024-11-25T16:00:00"
  }
]
```

---

## 6. 기타

### 6.1 홈페이지

**엔드포인트**: `GET /`

**설명**: 홈페이지 (HTML)

**응답**: HTML 페이지

### 6.2 사용자 정보 (임시)

**엔드포인트**: `GET /user`

**설명**: 사용자 정보 API (임시)

**성공 응답** (200 OK):
```json
{
  "message": "REST API 방식으로 구현됨",
  "status": "OK"
}
```

---

## 에러 응답 형식

모든 에러는 다음 형식으로 반환됩니다:

```json
{
  "code": "에러_코드",
  "message": "에러 메시지"
}
```

### 에러 코드

- `VALIDATION_ERROR`: 요청 값 검증 실패 (400)
- `KAKAO_API_ERROR`: 카카오 API 호출 실패 (502)
- `INTERNAL_ERROR`: 내부 서버 오류 (500)

---

## 보안 설정

현재 인증 없이 접근 가능한 경로:
- `/` (홈페이지)
- `/login/**` (로그인 페이지)
- `/api/auth/**` (인증 API)
- `/api/health` (헬스 체크)
- `/h2-console/**` (H2 콘솔, 개발용)

나머지 모든 경로는 인증이 필요합니다 (현재 미구현).

---

## 참고사항

- 모든 날짜/시간은 ISO 8601 형식입니다.
- 이미지 업로드는 `multipart/form-data` 형식을 사용합니다.
- 카카오 로그인은 OAuth2 인가 코드 플로우를 사용합니다.
- 현재 토큰은 UUID 기반 임시 토큰입니다. 운영 환경에서는 JWT로 교체 필요합니다.



