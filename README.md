# Ssak3 Backend

카카오 로그인 기능이 포함된 Spring Boot 백엔드 애플리케이션입니다.

## 🚀 시작하기

### 1. 카카오 개발자 설정

1. [Kakao Developers](https://developers.kakao.com)에서 애플리케이션 등록
2. 플랫폼 설정에서 Web 플랫폼 추가
3. Redirect URI 설정: `http://localhost:8080/api/login/oauth2/code/kakao`
4. 동의항목에서 필요한 권한 설정:
   - 프로필 정보 (닉네임)
   - 카카오계정(이메일)

### 2. 환경변수 설정

애플리케이션 실행 전에 다음 환경변수를 설정하세요:

```bash
# Windows (PowerShell)
$env:KAKAO_CLIENT_ID="your_kakao_client_id"
$env:KAKAO_CLIENT_SECRET="your_kakao_client_secret"

# Windows (CMD)
set KAKAO_CLIENT_ID=your_kakao_client_id
set KAKAO_CLIENT_SECRET=your_kakao_client_secret

# Linux/Mac
export KAKAO_CLIENT_ID=your_kakao_client_id
export KAKAO_CLIENT_SECRET=your_kakao_client_secret
```

### 3. 애플리케이션 실행

```bash
# Gradle을 사용한 실행
./gradlew bootRun

# 또는 JAR 파일 생성 후 실행
./gradlew build
java -jar build/libs/Ssak3_backend-1.0-SNAPSHOT.jar
```

### 4. 접속

- 메인 페이지: http://localhost:8080/api/
- 로그인 페이지: http://localhost:8080/api/login
- API 테스트: http://localhost:8080/api/user
- 헬스 체크: http://localhost:8080/api/health
- H2 데이터베이스 콘솔: http://localhost:8080/api/h2-console

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

- ✅ 카카오 OAuth2 로그인
- ✅ Spring Security 설정
- ✅ 사용자 정보 조회 API
- ✅ H2 인메모리 데이터베이스 (개발용)
- ✅ Thymeleaf 웹 페이지

## 📝 API 엔드포인트

- `GET /api/` - 메인 페이지
- `GET /api/login` - 로그인 페이지
- `GET /api/user` - 사용자 정보 조회 (JSON)
- `GET /api/health` - 헬스 체크
- `GET /oauth2/authorization/kakao` - 카카오 로그인 시작
- `POST /api/logout` - 로그아웃

## 🛠️ 기술 스택

- Java 17
- Spring Boot 3.2.0
- Spring Security
- Spring OAuth2 Client
- Spring Data JPA
- H2 Database
- Thymeleaf
- Gradle

## 🔒 보안 주의사항

- 카카오 클라이언트 시크릿은 환경변수로 관리
- 프로덕션 환경에서는 HTTPS 사용 권장
- H2 콘솔은 개발 환경에서만 사용
