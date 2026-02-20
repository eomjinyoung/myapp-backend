# VibeApp Backend (REST API Server)

Spring Security와 JWT를 기반으로 한 보안이 강화된 REST API 서버입니다.

## 주요 기능 (Key Features)

- **Post Management**: CRUD operations for posts with pagination and tagging.
- **User Authentication**: Signup, JSON-based login with JWT.
- **Security**: 
    - JWT Access/Refresh Token (with Rotation).
    - Redis-based Token Blacklisting.
    - Rate Limiting and Brute Force Protection.
    - Audit Logging (JSON Lines).
- **API Documentation**: OpenAPI 3.0 (Swagger UI).

## 기술 스택 (Tech Stack)

- **Java 25**
- **Spring Boot 4.0.1**
- **Spring Data JPA (Hibernate)**
- **Spring Security (JWT)**
- **Docker (PostgreSQL, Redis)**
- **H2 Database (Runtime)**

## 시작하기 (Getting Started)

### 인프라 실행 (Infrastructure)
PostgreSQL과 Redis를 실행하기 위해 Docker Compose를 사용합니다.
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

## API 주요 엔드포인트

### Post API
- `GET /api/posts`: 게시글 목록
- `POST /api/posts`: 게시글 등록

### User & Auth API
- `POST /api/signup`: 회원가입
- `POST /api/login`: 로그인
- `POST /api/logout`: 로그아웃