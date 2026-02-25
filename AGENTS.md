# AGENTS.md - Guideline for AI Agents

이 문서는 VibeApp 프로젝트를 보조하는 AI 에이전트를 위한 지침서입니다. 에이전트가 작업을 수행할 때 반드시 준수해야 할 핵심 지침을 정의합니다.

> [!IMPORTANT]
> 프로젝트의 상세 기능 및 기술 스택은 [README.md](file:///Users/eomjinyoung/git/myapp-backend/README.md)를 참고한다.

## 1. 코딩 가이드라인 (Coding Guidelines)
- **Build Tool**: Gradle 9.3.1 (Groovy DSL). **모든 Gradle 관련 태스크 및 명령어 실행 시 반드시 Gradle Wrapper(`./gradlew`)를 사용한다.**
- **DTO 규칙**: 
  - 모든 Data Transfer Object는 이름 끝에 `Dto` 접미사를 붙인다 (예: `UserSignupDto`, `PostResponseDto`).
  - 대소문자 표기법을 통일하고 불필요한 레거시 클래스 생성을 지양한다.
- **API 디자인**: 
  - RESTful 원칙을 준수한다.
  - 모든 응답은 JSON 형식을 따르며, 페이징 처리가 필요한 목록 조회 시 `page` 파라미터를 활용한다.
- **예외 처리**: 
  - `GlobalExceptionHandler`를 통해 모든 예외를 `ErrorResponseDto` 객체에 담아 반환한다.
  - 인증 실패 시 반드시 `401 Unauthorized`를 반환한다.

## 2. 문서화 및 커밋 컨벤션 (Docs & Commit Convention)
- **API 문서**: `springdoc-openapi`를 사용하여 Swagger UI를 자동 생성한다. 컨트롤러와 DTO에 명확한 어노테이션(@Operation, @Schema 등)을 추가한다.
- **Git Commit**: `docs/git-message-format.md`를 엄격히 준수한다.
  - 형식: `<type>(<scope>): <subject>`
  - Type: `feat`, `fix`, `refactor`, `docs`, `chore`, `test`, `style`, `perf`, `ci`, `revert`.
  - Subject: 50자 이내 명령문 ("~추가", "~변경").
  - Body: 무엇을(what), 왜(why) 변경했는지를 중심으로 작성한다.
  - **작업 완료 시**: 각 작업을 마칠 때마다 **작업 내용의 정상 동작을 확인(검증)한 후**, 정의된 형식에 맞춰 `git commit` 및 `git push`를 수행한다.

## 3. 에이전트 주의사항 (Special Notes for Agents)
- **프론트엔드 코드**: 프로젝트 내에서 모든 HTML/Thymeleaf 등 프론트엔드 관련 코드는 제거되었으므로, 관련 코드를 생성하거나 제안하지 않는다.
- **설정 변경**: `application.yml`이나 `SecurityConfig.java` 수정 시 기존 보안 정책(블랙리스트, Rotation 등)이 깨지지 않도록 주의한다.
- **자율성**: 최신 Java 및 Spring Boot 기능을 활용하여 최적의 솔루션을 제안하되, 가독성을 최상으로 유지한다.
