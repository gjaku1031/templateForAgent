# PR 작업 계획 (처음부터 순차 구축 → 파일 단위 커밋)

모든 파일이 스테이징된 현 상태를 “처음부터 실제 개발” 순서로 재구성해 파일 단위로 커밋합니다. 원칙은 “파일 1개당 커밋 1개”이나, Gradle Wrapper 등은 관례상 묶음 커밋 허용.

브랜치
- `git checkout -b feat/bootstrap-initial-setup` (이슈 미사용이므로 설명형 브랜치명)

커밋 컨벤션
- `document/plan/git Convention.md` 따름. 이슈 번호 표기는 생략.

실무 순서 커밋 플로우

0) 계획/가이드 문서
- docs(plan): PR 작업 계획 추가 → `document/plan/pr TodoList.md`
- docs(study): 레이어/Swagger/Paging/@CurrentUser/enum/LXDocker 문서 추가/유지 →
  - `document/study/DDD Layered Architecture.md`
  - `document/study/Swagger Security 문서화.md`
  - `document/study/Paging.md`
  - `document/study/@CurrentUser.md`
  - `document/study/enum은 Lombok이 아닌  명시적 생성자를 두는게 안전.md`
  - `document/study/Docker Compose 작성.md`

1) Gradle/프로젝트 부트스트랩
- build: Gradle wrapper 추가(묶음) → `gradlew`, `gradlew.bat`, `gradle/wrapper/*`
- build: settings.gradle 초기화 → `settings.gradle`
- build: 기본 빌드 스크립트 작성 → `build.gradle`
- build: gitattributes/gitignore 정리 → `.gitattributes`, `.gitignore`

2) 앱 구동 스켈레톤/프로필 설정
- feat(app): 스프링 부트 메인 클래스 → `src/main/java/.../TemplateForAgentApplication.java`
- config: 프로필/환경 설정 → `src/main/resources/application.yml`
- config: 로컬/운영 프로필 → `src/main/resources/application-local.yml`, `src/main/resources/application-prod.yml`
- ops: 로컬 인프라(docker compose) → `docker-compose.yml`

3) 공통 에러 인프라
- feat(common): ErrorCode 정의 → `common/error/ErrorCode.java`
- feat(common): ErrorResponse DTO → `presentation/common/ErrorResponse.java`
- feat(common): 도메인 예외들 → `common/error/exception/*.java`
  - `BusinessException.java`, `NotFoundException.java`, `UnauthorizedException.java`
- feat(common): 전역 예외 처리기 → `presentation/common/GlobalExceptionHandler.java`

4) 인프라스트럭처 설정(웹/JPA/Redis/Swagger/QueryDSL/CORS/JWT 설정)
- config: JPA → `infrastructure/config/JpaConfig.java`
- config: Redis → `infrastructure/config/RedisConfig.java`
- config: OpenAPI/Swagger → `infrastructure/config/OpenApiConfig.java`
- config: QueryDSL → `infrastructure/config/QuerydslConfig.java`
- config: CORS → `infrastructure/config/CorsConfig.java`
- config: WebMvc 설정(아규먼트 리졸버 등록 등) → `infrastructure/config/WebMvcConfig.java`
- config: JWT 바인딩 설정 → `infrastructure/config/JwtConfig.java`

5) 도메인 공통 베이스 & 유저 도메인
- feat(domain): BaseTimeEntity → `domain/BaseTimeEntity.java`
- feat(user): User 엔티티 → `domain/user/User.java`
- feat(user): UserRepository → `domain/user/UserRepository.java`

6) 보드 도메인(+QueryDSL 커스텀)
- feat(board): Board 엔티티 → `domain/board/Board.java`
- feat(board): BoardRepository → `domain/board/BoardRepository.java`
- feat(board): BoardRepositoryCustom → `domain/board/BoardRepositoryCustom.java`
- feat(board): BoardRepositoryImpl(QueryDSL) → `domain/board/BoardRepositoryImpl.java`

7) 시큐리티(도메인 권한/인증)
- feat(security): CustomUserDetails → `infrastructure/security/CustomUserDetails.java`
- feat(security): CustomUserDetailsService → `infrastructure/security/CustomUserDetailsService.java`
- feat(security): PermissionEvaluator → `infrastructure/security/evaluator/AppPermissionEvaluator.java`
- feat(security): 인증 실패/인가 실패 핸들러 →
  - `infrastructure/security/handler/AuthenticationEntryPointImpl.java`
  - `infrastructure/security/handler/AccessDeniedHandlerImpl.java`
- feat(security): JWT Provider/Util/Filter →
  - `infrastructure/security/jwt/JwtProvider.java`
  - `infrastructure/security/jwt/JwtUtil.java`
  - `infrastructure/security/jwt/JwtFilter.java`
- feat(security): 메서드 보안/필터 체인 설정 → `infrastructure/config/SecurityConfig.java`

8) 프레젠테이션 유틸(인증 사용자 주입/애노테이션)
- feat(web): @CurrentUser 애노테이션 → `presentation/common/annotation/CurrentUser.java`
- feat(web): CurrentUserArgumentResolver → `infrastructure/web/CurrentUserArgumentResolver.java`
- feat(web): 권한 애노테이션 묶음 → `presentation/common/annotation/*.java`
  - `IsAuthenticated`, `IsAdmin`, `AdminOrUserSelfRead/Update/Delete`, `AdminOrBoardOwnerUpdate/Delete`

9) 애플리케이션 서비스
- feat(service-auth): AuthService → `application/auth/AuthService.java`
- feat(service-user): UserService → `application/user/UserService.java`
- feat(service-board): BoardService → `application/board/BoardService.java`

10) DTO/프레젠테이션(유저)
- feat(user-dto): 로그인/토큰 DTO → `presentation/user/dto/{LoginRequest, TokenResponse, RefreshRequest}.java`
- feat(user-dto): 유저 CRUD DTO → `presentation/user/dto/{UserCreateRequest, UserRegisterRequest, UserUpdateRequest, UserResponse}.java`
- docs(user): 문서 인터페이스(보안 요구) → `presentation/user/docs/{UserApiDocs,UserAuthApiDocs}.java`
- feat(user-api): UserController → `presentation/user/UserController.java`

11) DTO/프레젠테이션(보드)
- feat(board-dto): Board DTO → `presentation/board/dto/{BoardCreateRequest, BoardUpdateRequest, BoardResponse}.java`
- docs(board): 문서 인터페이스(보안 요구/응답) → `presentation/board/docs/BoardApiDocs.java`
- feat(board-api): BoardController → `presentation/board/BoardController.java`

12) 문서/도구
- docs: AGENTS.md 반영/검토 → `AGENTS.md`
- test: 스켈레톤 테스트(빌드 확인) → `src/test/java/.../TemplateForAgentApplicationTests.java`

정책/검증 체크리스트
- 빌드: `./gradlew clean build -x test` 성공
- Swagger: `bearerAuth` 스킴 노출 및 보호 엔드포인트 보안 요구 표시
- 인증/인가 실패 시 통일된 `ErrorResponse` 포맷 응답 확인(401/403 핸들러)
- QueryDSL 페이징: 컬렉션 fetch join 금지, count 지연, `Page.map`으로 DTO 변환(서비스)
- 컨트롤러 얇게: 서비스에서 DTO Page 반환, 컨트롤러는 호출/반환만

PR 메시지(초안)
- 제목: `feat, docs, refactor: 초기 부트스트랩/도메인/시큐리티/JWT 구축 및 문서화`
- 본문:
  - Gradle/프로필/Infra 설정 부트스트랩
  - 공통 에러 인프라/전역 예외 처리
  - 도메인(User/Board) + QueryDSL 레포지토리
  - 시큐리티(JWT/PermissionEvaluator/핸들러/필터 체인)
  - 서비스(User/Board/Auth) + 컨트롤러/DTO + Swagger 분리 문서
  - 페이징/Swagger 보안 문서 추가, @CurrentUser 가이드 포함
