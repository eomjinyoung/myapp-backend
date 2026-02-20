# Project Specification: VibeApp

최소 기능 스프링부트 애플리케이션을 생성하는 프로젝트 명세서입니다. 본 프로젝트는 순수 REST API 서버 아키텍처를 기반으로 합니다.

## 1. 프로젝트 설정 (Project Settings)

- **JDK**: JDK 25 이상
- **Language**: Java
- **Spring Boot**: 4.0.1 이상
- **Build Tool**: Gradle 9.3.0 이상 (Groovy DSL 사용)
- **Dependencies**: 
    - `spring-boot-starter-web`
    - `spring-boot-starter-validation`
    - `spring-boot-starter-data-jpa`
    - `spring-boot-starter-security`
    - `com.fasterxml.jackson.core:jackson-databind`
    - `spring-boot-starter-data-redis`
    - `spring-boot-starter-aspectj`
    - `net.logstash.logback:logstash-logback-encoder:8.0`
    - `io.jsonwebtoken:jjwt-api:0.12.6` (and impl/jackson)
    - `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1`
    - `h2` (runtime)

## 2. 플러그인 (Plugins)

- **Dependency Management**: `io.spring.dependency-management`
    - Spring Boot 버전에 맞춰 자동 추가

## 3. 프로젝트 메타데이터 (Project Metadata)

- **Group**: `com.example.vibeapp`
- **Artifact**: `vibeapp`
- **Main Class Name**: `VibeApp`
- **Description**: Spring Security와 JWT 기반의 보안이 강화된 세련된 REST API 서버이다.
- **Configuration**: YAML 파일 (`application.yml`) 사용

## 4. 인프라 및 보안 (Infrastructure & Security)

- **API Documentation**: OpenAPI/Swagger UI (`/swagger-ui/index.html`)
- **Infrastructure**: Docker Compose를 이용한 PostgreSQL 및 Redis 환경 구성.
- **Persistence**: Spring Data JPA (`JpaRepository`)와 H2(또는 PostgreSQL) Database 사용.
- **Security**: Spring Security와 JWT를 통한 무상태(Stateless) 인증 관리.
    - 비밀번호 암호화: `BCryptPasswordEncoder` 사용.
    - 인증 방식: `JwtTokenProvider`와 `JwtAuthenticationFilter`를 이용한 JWT 기반 인증.
    - Access Token 무효화: 로그아웃 시 Redis를 블랙리스트로 활용하여 토큰을 즉시 무효화함 (TTL 자동 관리).
    - Replay Attack 탐지: 이미 회전(Rotation)된 리프레시 토큰의 재사용 시도를 감지하면 해당 사용자의 모든 리프레시 토큰을 무효화(Global Purge) 처리함.
    - Refresh Token: RDB(H2)에 저장하며, 만료 시 또는 재발급 시 갱신(Rotation) 정책 적용.
    - Rate Limiting: Redis를 사용하여 동일 IP에서의 과도한 API 요청(로그인, 회원가입 등)을 차단함 (429 Too Many Requests).
    - Brute Force 방어: 5회 연속 로그인 실패 시 15분간 해당 계정을 잠금 처리함 (403 Forbidden).
    - Audit Logging: AOP와 @AuditLog 어노테이션을 사용하여 주요 보안 이벤트(로그인, 로그아웃, 재발급, 비번변경)를 `logs/audit.log`에 JSON Lines 형식으로 기록.
    - CSRF/Login/Logout: 비활성화 (무상태 인증 아키텍처이므로 `csrf`, `formLogin`, `httpBasic`, `logout` 비활성화).
    - H2 Console: 접근을 위한 프레임 옵션 설정 (`sameOrigin`).
- **Exception Handling**: 모든 예외는 `ErrorResponseDto` 객체에 담겨 JSON 형식으로 반환됨. 인증 실패 시 401 Unauthorized 응답.

## 5. 엔드포인트 (Endpoints)

### 5.1. REST API (Data)
- **Post API (`/api/posts`)**
    - `GET /api/posts?page=1`: 게시글 목록 조회 (JSON, 페이징 포함)
    - `GET /api/posts/{no}`: 게시글 상세 조회 (JSON)
    - `POST /api/posts`: 게시글 등록 (JSON Request Body)
    - `PATCH /api/posts/{no}`: 게시글 수정 (JSON Request Body, 부분 업데이트)
    - `DELETE /api/posts/{no}`: 게시글 삭제
- **User API**
    - `POST /api/signup`: 회원가입 처리 (JSON Request Body)
    - `GET /api/user/me`: 현재 로그인한 사용자 정보 조회
    - `POST /api/user/password`: 비밀번호 변경 (JSON Request Body, 인증 필요)
- **Auth API**
    - `POST /api/login`: 로그인 처리 (JSON Request Body, returns Access & Refresh Token)
    - `POST /api/reissue`: 토큰 재발급 (JSON Request Body, Refresh Token Rotation 적용)
    - `POST /api/logout`: 로그아웃 처리 (DB에서 Refresh Token 삭제)
    - Logout is handled by calling `/api/logout`.

## 6. 주요 파일 구조 (Project Structure)

- `src/main/java/com/example/vibeapp/VibeApp.java`: 메인 클래스
- `src/main/java/com/example/vibeapp/post/Post.java`: 게시글 엔티티
- `src/main/java/com/example/vibeapp/post/PostController.java`: 게시글 REST 컨트롤러 (`@RestController`)
- `src/main/java/com/example/vibeapp/user/UserController.java`: 사용자 REST 컨트롤러 (`@RestController`)
- `src/main/java/com/example/vibeapp/config/SecurityConfig.java`: Spring Security 설정 및 JSON 로그인 핸들러
- `src/main/java/com/example/vibeapp/config/GlobalExceptionHandler.java`: 전역 에러 처리 (`@RestControllerAdvice`)
- `src/main/resources/application.yml`: 애플리케이션 설정
- `src/main/java/com/example/vibeapp/config/RedisConfig.java`: Redis 연동 설정
- `src/main/java/com/example/vibeapp/config/RateLimitInterceptor.java`: IP 기반 요청 제한 인터셉터
- `src/main/java/com/example/vibeapp/security/AuditLog.java`: Audit 로그 어노테이션
- `src/main/java/com/example/vibeapp/security/AuditLogAspect.java`: AOP 기반 Audit 로그 처리기
- `src/main/java/com/example/vibeapp/security/TokenBlacklistService.java`: 블랙리스트 및 재사용 감지 서비스
- `src/main/java/com/example/vibeapp/security/LoginAttemptService.java`: 로그인 실패 및 계정 잠금 관리 서비스
- `src/main/java/com/example/vibeapp/security/RateLimiterService.java`: Redis 기반 Rate Limit 처리 서비스
- `docker-compose.yml`: Redis 및 PostgreSQL 인프라 설정
- `docs/git-message-format.md`: Git 커밋 메시지 규칙 정의 문서


## 8. 현재 프로젝트 상태 (Current Status)

- [x] JWT 기반 무상태(Stateless) 인증 시스템 구현 및 최적화 완료
- [x] Backend: Refresh Token 저장 및 관리를 위한 RDB 연동 완료
- [x] Backend: Refresh Token Rotation 정책이 적용된 토큰 재발급 API 구현 완료
- [x] Backend: 서버 측 로그아웃(`/api/logout`)을 통한 리프레시 토큰 무효화 구현 완료
- [x] Backend: Redis 기반 Access Token 블랙리스트를 통한 즉시 무효화 기능 구현 완료
- [x] Backend: Refresh Token 재사용 공격(Replay Attack) 탐지 및 자동 세션 차단 기능 구현 완료
- [x] Backend: Brute Force 공격 방어(Rate Limiting, 계정 잠금) 기능 구현 및 검증 완료
- [x] Backend: AOP 기반 보안 이벤트 Audit Logging 기능 (JSON Lines) 구현 완료
- [x] Backend: 사용자 비밀번호 변경 기능 (`/api/user/password`) 구현 완료
- [x] Backend: DTO 명명 규칙 표준화 (`Dto` 접미사 사용 및 대소문자 통일) 완료
- [x] Backend: OpenAPI/Swagger UI 연동 및 스키마 명세 최적화 완료
- [x] Backend: 불필요한 레거시 코드 (`UserLoginDto` 등) 정리 및 구조 최적화 완료
- [x] `GlobalExceptionHandler`를 통한 인증 및 유효성 검사 예외 처리 완료 (ErrorResponseDto 기반)
- [x] Git 커밋 메시지 컨벤션 정의 (`docs/git-message-format.md`) 및 적용 완료
- [x] 서비스 아키텍처를 순수 REST API 서버로 전환 (프론트엔드 코드 제거) 완료


