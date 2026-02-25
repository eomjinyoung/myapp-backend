# VibeApp Backend (REST API Server)

Spring Security와 JWT를 기반으로 한 보안이 강화된 세련된 REST API 서버입니다. 이 서버는 순수 REST API 아키텍처를 기반으로 하며, 무상태(Stateless) 인증을 제공합니다.

## 1. 기술 스택 (Tech Stack)

- **JDK**: 25 이상
- **Language**: Java
- **Spring Boot**: 4.0.1 이상
- **Build Tool**: Gradle 9.3.1 (Groovy DSL)
- **Infrastructure**: Docker Compose (Redis), H2 Database (Local Runtime)
- **Persistence**: Spring Data JPA (`JpaRepository`)
- **Security**: Spring Security, JWT (jjwt 0.12.6)
- **Documentation**: OpenAPI 3.0 (Swagger UI)
- **Logging**: Logback (JSON Lines format via logstash-logback-encoder)

## 2. 주요 기능 및 보안 프로토콜 (Key Features & Security)

- **게시글 관리 (Post Management)**: 페이징 및 태깅 기능이 포함된 게시글 CRUD.
- **사용자 인증 (User Authentication)**: 
    - JWT Access/Refresh Token 기반 무상태 인증.
    - **Refresh Token Rotation**: 토큰 재발급 시 리프레시 토큰 갱신 정책 적용.
    - **Token Blacklisting**: 로그아웃 시 Redis를 블랙리스트로 활용하여 Access Token 즉시 무효화.
- **보안 방어 (Security Protections)**:
    - **Replay Attack 탐지**: 이미 사용된 리프레시 토큰 재사용 시 해당 사용자의 모든 세션 무효화 (Global Purge).
    - **Rate Limiting**: Redis 기반 IP별 API 요청 제한 (로그인, 회원가입 등).
    - **Brute Force 방어**: 5회 로그인 실패 시 15분간 계정 잠금.
- **감사 로그 (Audit Logging)**: AOP 기반으로 주요 보안 이벤트를 `logs/audit.log`에 JSON Lines 형식으로 기록.
- **CORS 설정**: `http://localhost:3000`으로부터의 요청 허용.

## 3. 시작하기 (Getting Started)

### 인프라 실행 (Infrastructure)
Redis를 실행하기 위해 Docker Compose를 사용합니다.
```bash
docker-compose up -d
```

### 애플리케이션 실행 (Application)
```bash
./gradlew bootRun
```

### API 문서 확인 (API Documentation)
애플리케이션 실행 후 아래 주소에서 Swagger UI를 확인할 수 있습니다.
- `http://localhost:8080/swagger-ui/index.html`

## 4. API 주요 엔드포인트 (API Endpoints)

### Post API (`/api/posts`)
- `GET /api/posts?page=1`: 게시글 목록 조회 (JSON, 페이징 포함)
- `GET /api/posts/{no}`: 게시글 상세 조회
- `POST /api/posts`: 게시글 등록
- `PATCH /api/posts/{no}`: 게시글 수정 (부분 업데이트)
- `DELETE /api/posts/{no}`: 게시글 삭제

### User & Auth API
- **User**
    - `POST /api/signup`: 회원가입
    - `GET /api/user/me`: 내 정보 조회
    - `POST /api/user/password`: 비밀번호 변경
- **Auth**
    - `POST /api/login`: 로그인 (Access & Refresh Token 발급)
    - `POST /api/reissue`: 토큰 재발급
    - `POST /api/logout`: 로그아웃 (서버 세션/토큰 무효화)

## 5. 현재 프로젝트 상태 (Project Status)
- [x] JWT 기반 무상태 인증 시스템 구현 및 최적화 완료
- [x] Refresh Token Rotation 및 Replay Attack 탐지 기능 구현 완료
- [x] Redis 기반 토큰 블랙리스트 및 Rate Limiting 구현 완료
- [x] AOP 기반 보안 이벤트 Audit Logging 구현 완료
- [x] OpenAPI/Swagger UI 연동 완료
- [x] 서비스 아키텍처 순수 REST API 전환 완료