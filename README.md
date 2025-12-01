# Ssak3 Backend

Spring Boot 백엔드 애플리케이션입니다.

## 🚀 시작하기

### 1. 로컬 개발 환경 실행

```bash
# Gradle을 사용한 실행
./gradlew bootRun

# 또는 JAR 파일 생성 후 실행
./gradlew build
java -jar build/libs/*.jar
```

### 2. 접속

- 메인 페이지: http://localhost:8080/
- 헬스 체크: http://localhost:8080/api/health
- H2 데이터베이스 콘솔: http://localhost:8080/h2-console

## ☁️ Render 배포

### 1. GitHub에 코드 Push

```bash
git add .
git commit -m "Initial commit"
git push origin main
```

### 2. Render 설정

1. [Render.com](https://render.com) 접속 및 로그인
2. "New +" → "Web Service" 선택
3. GitHub 저장소 연결 및 선택
4. 다음 설정 입력:
   - **Name**: `ssak3-backend` (원하는 이름)
   - **Environment**: `Java`
   - **Build Command**: `./gradlew build`
   - **Start Command**: `java -jar build/libs/*.jar`
   - **Health Check Path**: `/api/health`

### 3. 환경 변수 설정 (선택사항)

Render 대시보드에서 환경 변수 추가:
- `SPRING_PROFILES_ACTIVE`: `production`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update`
- `SPRING_JPA_SHOW_SQL`: `false`

### 4. 데이터베이스 설정 (선택사항)

프로덕션 환경에서는 PostgreSQL 사용 권장:
1. Render에서 "New +" → "PostgreSQL" 생성
2. 생성된 데이터베이스의 내부 데이터베이스 URL 복사
3. 환경 변수에 `DATABASE_URL` 추가

### 5. 배포 완료

설정 완료 후 "Create Web Service" 클릭하면 자동으로 빌드 및 배포가 시작됩니다.
약 10-15분 후 배포 완료!

## 📁 프로젝트 구조

```
src/main/java/org/example/
├── Main.java                 # Spring Boot 메인 애플리케이션
├── config/
│   └── SecurityConfig.java   # Spring Security 설정
└── controller/
    └── HomeController.java   # 웹 컨트롤러

src/main/resources/
├── application.yml           # 애플리케이션 설정
└── templates/               # Thymeleaf 템플릿
    ├── home.html
    └── login.html
```

## 🔧 주요 기능

- ✅ 상품 관리 (등록, 조회, 수정, 삭제)
- ✅ 카테고리 관리
- ✅ 찜 기능
- ✅ 1대1 채팅 기능
- ✅ 사용자 관리
- ✅ Spring Security 설정
- ✅ H2 인메모리 데이터베이스 (개발용)
- ✅ Thymeleaf 웹 페이지

## 📝 주요 API 엔드포인트

### 상품
- `GET /api/products` - 전체 상품 조회
- `GET /api/products/{id}` - 상품 상세 조회
- `POST /api/products/with-upload` - 상품 등록 (이미지 포함)
- `PUT /api/products/{id}` - 상품 수정
- `DELETE /api/products/{id}` - 상품 삭제

### 카테고리
- `GET /api/categories` - 전체 카테고리 조회
- `POST /api/categories` - 카테고리 생성

### 찜
- `POST /api/likes?userId={userId}&productId={productId}` - 찜 추가
- `DELETE /api/likes?userId={userId}&productId={productId}` - 찜 취소
- `GET /api/likes/user/{userId}` - 사용자 찜 목록

### 채팅
- `POST /api/chat/rooms` - 채팅방 생성/조회
- `GET /api/chat/rooms/user/{userId}` - 사용자 채팅방 목록
- `POST /api/chat/rooms/{chatRoomId}/messages` - 메시지 전송
- `GET /api/chat/rooms/{chatRoomId}/messages` - 메시지 목록 조회

### 사용자
- `GET /api/users` - 전체 사용자 조회
- `GET /api/users/{id}` - 사용자 조회
- `POST /api/users` - 사용자 등록

### 기타
- `GET /` - 메인 페이지
- `GET /api/health` - 헬스 체크

## 🛠️ 기술 스택

- Java 17
- Spring Boot 3.5.7
- Spring Security
- Spring Data JPA
- H2 Database (개발용) / PostgreSQL (프로덕션)
- Thymeleaf
- Gradle

## 🔒 보안 주의사항

- 프로덕션 환경에서는 HTTPS 사용 권장
- H2 콘솔은 개발 환경에서만 사용
- 환경 변수로 민감한 정보 관리
- 프로덕션에서는 PostgreSQL 등 영구 데이터베이스 사용 권장
