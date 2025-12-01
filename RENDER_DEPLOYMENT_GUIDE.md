# Render 배포 전체 가이드

## 📋 준비된 파일 목록

### ✅ 필수 파일
- ✅ `render.yaml` - Render 자동 배포 설정
- ✅ `Dockerfile` - Docker 이미지 빌드 설정
- ✅ `gradlew` - 실행 권한 설정 완료 (100755)
- ✅ `application-production.yml` - 프로덕션 환경 설정
- ✅ `application.yml` - 개발 환경 설정 (PORT 환경 변수 지원)

---

## 🔧 파일별 설정 내용

### 1. render.yaml
```yaml
services:
  - type: web
    name: ssak3-backend
    dockerfilePath: ./Dockerfile
    dockerContext: .
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: production
      - key: SPRING_JPA_HIBERNATE_DDL_AUTO
        value: update
      - key: SPRING_JPA_SHOW_SQL
        value: false
    healthCheckPath: /api/health
```

**설명:**
- Docker를 사용하여 배포
- 프로덕션 프로파일 자동 활성화
- JPA DDL 자동 업데이트
- 헬스 체크 경로 설정

---

### 2. Dockerfile
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew build -x test --no-daemon
RUN find build/libs -name "*.jar" ! -name "*-plain.jar" -type f | head -1 | xargs -I {} cp {} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**설명:**
- Java 17 사용
- Gradle로 빌드
- 테스트 제외
- JAR 파일 자동 실행

---

### 3. application.yml (개발용)
```yaml
server:
  port: ${PORT:8080}  # Render PORT 환경 변수 지원

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

---

### 4. application-production.yml (프로덕션용)
```yaml
server:
  port: ${PORT:8080}

spring:
  datasource:
    url: ${DATABASE_URL:jdbc:h2:mem:testdb}
    driver-class-name: ${DB_DRIVER:org.h2.Driver}
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:password}
  
  h2:
    console:
      enabled: false  # 프로덕션에서는 비활성화
  
  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
    show-sql: ${SPRING_JPA_SHOW_SQL:false}

logging:
  level:
    org.example: INFO
    org.springframework.web: INFO
    org.hibernate: WARN
```

---

## 🚀 Render 대시보드 설정

### 방법 1: render.yaml 자동 인식 (권장)

1. **GitHub에 코드 Push**
   ```bash
   git add .
   git commit -m "Add Render deployment configuration"
   git push origin main
   ```

2. **Render 대시보드 접속**
   - https://dashboard.render.com 접속
   - 로그인

3. **새 Web Service 생성**
   - "New +" 버튼 클릭
   - "Web Service" 선택
   - GitHub 저장소 연결 및 선택

4. **자동 설정 확인**
   - render.yaml 파일이 있으면 자동으로 인식됩니다
   - 다음 설정이 자동으로 적용됩니다:
     - **Name**: `ssak3-backend`
     - **Environment**: `Docker` (자동)
     - **Dockerfile Path**: `./Dockerfile`
     - **Health Check Path**: `/api/health`

5. **환경 변수 확인**
   - `SPRING_PROFILES_ACTIVE`: `production` (자동 설정)
   - `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update` (자동 설정)
   - `SPRING_JPA_SHOW_SQL`: `false` (자동 설정)

6. **배포 시작**
   - "Create Web Service" 클릭
   - 자동으로 빌드 및 배포 시작
   - 약 10-15분 소요

---

### 방법 2: 수동 설정 (render.yaml이 인식되지 않는 경우)

1. **기본 설정**
   - **Name**: `ssak3-backend`
   - **Environment**: `Docker` ⚠️ **중요: Node가 아닌 Docker 선택**
   - **Region**: `Oregon (US West)` 또는 원하는 지역
   - **Branch**: `main`

2. **Docker 설정**
   - **Dockerfile Path**: `./Dockerfile`
   - **Docker Context**: `.` (현재 디렉토리)

3. **환경 변수 추가**
   ```
   SPRING_PROFILES_ACTIVE = production
   SPRING_JPA_HIBERNATE_DDL_AUTO = update
   SPRING_JPA_SHOW_SQL = false
   ```

4. **고급 설정**
   - **Health Check Path**: `/api/health`
   - **Auto-Deploy**: `Yes` (GitHub push 시 자동 배포)

---

## 🗄️ 데이터베이스 설정 (선택사항)

### PostgreSQL 사용 (권장)

1. **PostgreSQL 생성**
   - Render 대시보드에서 "New +" → "PostgreSQL" 선택
   - 이름 설정 후 생성

2. **내부 데이터베이스 URL 복사**
   - 생성된 PostgreSQL 서비스에서 "Internal Database URL" 복사
   - 예: `postgresql://user:password@host:5432/dbname`

3. **환경 변수 추가**
   - Web Service의 Environment Variables에 추가:
     ```
     DATABASE_URL = postgresql://user:password@host:5432/dbname
     DB_DRIVER = org.postgresql.Driver
     ```

4. **build.gradle에 PostgreSQL 드라이버 추가** (이미 있음)
   ```gradle
   runtimeOnly 'org.postgresql:postgresql'
   ```

---

## ✅ 배포 확인

### 1. 배포 상태 확인
- Render 대시보드에서 "Logs" 탭 확인
- 빌드 및 배포 로그 확인

### 2. 헬스 체크
- 배포 완료 후 제공된 URL로 접속
- `https://your-app.onrender.com/api/health` 접속
- `{"status":"OK","message":"Ssak3 Backend is running!"}` 응답 확인

### 3. API 테스트
- `GET https://your-app.onrender.com/api/products` - 상품 목록 조회
- `GET https://your-app.onrender.com/api/categories` - 카테고리 목록 조회

---

## 🔍 문제 해결

### 문제 1: "Permission denied" 오류
**해결:**
```bash
git update-index --chmod=+x gradlew
git commit -m "Add execute permission to gradlew"
git push origin main
```

### 문제 2: Language가 Node로 설정됨
**해결:**
- 기존 서비스 삭제 후 재생성
- Environment를 "Docker"로 선택

### 문제 3: 빌드 실패
**확인 사항:**
- Dockerfile 경로 확인
- gradlew 실행 권한 확인
- build.gradle 의존성 확인

### 문제 4: 포트 오류
**확인 사항:**
- `application.yml`에서 `port: ${PORT:8080}` 설정 확인
- Render가 자동으로 PORT 환경 변수를 제공

---

## 📝 체크리스트

배포 전 확인:
- [ ] `render.yaml` 파일 존재
- [ ] `Dockerfile` 파일 존재
- [ ] `gradlew` 실행 권한 설정 (100755)
- [ ] `application-production.yml` 파일 존재
- [ ] GitHub에 모든 파일 push 완료
- [ ] Render에서 Environment가 "Docker"로 설정됨
- [ ] 환경 변수 설정 완료
- [ ] 헬스 체크 경로 설정 완료

---

## 🎯 최종 설정 요약

| 항목 | 값 |
|------|-----|
| **Environment** | Docker |
| **Dockerfile Path** | `./Dockerfile` |
| **Build Command** | (Dockerfile에서 자동 처리) |
| **Start Command** | (Dockerfile에서 자동 처리) |
| **Health Check** | `/api/health` |
| **Port** | 8080 (자동) |
| **Java Version** | 17 |
| **Spring Profile** | production |

---

## 📞 추가 도움말

- Render 공식 문서: https://render.com/docs
- Docker 문서: https://docs.docker.com
- Spring Boot 배포 가이드: https://spring.io/guides/gs/spring-boot-for-azure/

