# Repository Guidelines

이 문서는 본 저장소의 기여자를 위한 실무 지침입니다. 프로젝트는 Spring Boot(자바 17) + Gradle을 사용하며 PostgreSQL, Redis, Spring Security(JWT), Springdoc OpenAPI를 포함합니다.

## 프로젝트 구조 & 모듈 구성
- `src/main/java`: 애플리케이션 코드
  - `application/`: 유스케이스/서비스
  - `domain/`: 엔티티, 리포지토리, 도메인 로직
  - `infrastructure/`: 설정, 보안, 외부 어댑터(JWT, Redis 등)
  - `presentation/`: 컨트롤러, DTO, API 문서
- `src/main/resources`: 설정 파일(`application.yml`, 프로필)
- `src/test/java`: 테스트(JUnit + Spring Boot Test)
- `document/`: 보조 문서
- `docker-compose.yml`: 로컬 Postgres/Redis 실행

## 빌드·테스트·개발 실행
- `./gradlew build` — 컴파일, 테스트, JAR 생성
- `./gradlew test` — 단위/통합 테스트 실행
- `./gradlew bootRun` — 애플리케이션 실행(기본 프로필 `local`)
- `docker compose up -d` — 로컬 Postgres/Redis 기동
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 코딩 스타일 & 네이밍
- 자바 17, 4칸 들여쓰기, UTF-8
- 클래스 `PascalCase`, 메서드/필드 `camelCase`, 상수 `UPPER_SNAKE_CASE`
- 계층별 패키징(`application`, `domain`, `infrastructure`, `presentation`), 기능별 DTO는 하위 패키지에 배치
- 생성자 주입 선호(Lombok `@RequiredArgsConstructor`), 컨트롤러는 얇게 유지

## 주석 컨벤션
- 문장체: 한국어 명령/간결체 사용(예: “아래 호스트로 접속”), 존칭/군더더기 금지
- 클래스: 역할·책임을 한두 문장으로 요약하고, 필요 시 섹션을 추가
  - 예외 섹션: `<p><b>예외</b></p> + <ul><li>...</li></ul>`
  - 설계 메모: 트랜잭션/동시성/성능/보안 의도 요약
  - 관련 타입: `@see`로 연관 도메인/리포지토리/DTO 연결
- 메서드: 한 줄 요약 + 상세 설명 + 태그로 구조화
  - 상세: 처리 흐름, 트랜잭션/롤백, 사전조건/사후조건
  - 태그: `@param`/`@return`/`@throws` (IntelliJ Javadoc 형식)
- 사소한 private 메서드는 생략 가능하나, 비즈니스 결정·트랜잭션 경계·보안 요구는 반드시 기술
- 라인 길이 ~100자 권장, 필요한 경우 `<p>`/`<ul>` 등 HTML 블록 활용
- Swagger 전용 apidocs 인터페이스에는 Javadoc을 달지 않음(어노테이션만 유지)

예시 — 클래스 주석(발췌)
```
/**
 * 게시판( Board ) 도메인의 애플리케이션 서비스
 * <p>CRUD·검색을 담당하고 리포지토리를 통해 엔티티를 조작.</p>
 *
 * <p><b>예외</b></p>
 * <ul>
 *   <li>NotFoundException: 대상이 없을 때</li>
 * </ul>
 *
 * <p><b>설계 메모</b></p>
 * <ul>
 *   <li>stateless 서비스, 트랜잭션은 메서드 단위</li>
 * </ul>
 * @see BoardRepository
 */
```

예시 — 메서드 주석(발췌)
```
/**
 * 게시글을 생성하고 생성된 게시글 ID 반환.
 * <p>작성자를 조회→엔티티 저장. 예외 시 롤백.</p>
 * @param authorId 작성자 ID
 * @param title    제목
 * @param content  내용
 * @return 생성된 게시글 ID
 * @throws NotFoundException 작성자가 없을 때
 */
```

## 테스트 가이드
- 프레임워크: JUnit 5, Spring Boot Test
- 테스트 이름은 대상 클래스에 대응(e.g., `UserServiceTest`)
- 슬라이스 테스트: `@DataJpaTest`, `@WebMvcTest`; 통합: `@SpringBootTest`
- 실행: `./gradlew test`

## 커밋 & PR 가이드
- 커밋: 명령형, 간결한 제목, 필요시 본문에 근거/맥락 추가
  - 예: `fix(security): JWT 필터에서 무효 토큰 무시`
- PR: 변경 요약, 연관 이슈, 재현/검증 방법, API/문서 변경 시 스크린샷
- 작은 단위로 제출, 동작/설정 변경 시 문서 갱신

## 보안 & 설정 팁
- 프로필: 기본 `local`, 운영 `prod`(환경변수 `SPRING_PROFILES_ACTIVE`)
- 환경변수: `POSTGRES_*`, `REDIS_*`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`
- 로컬: `docker compose up -d` 후 `./gradlew bootRun`
- JWT 발급자/만료는 `application.yml`, 시크릿은 프로필별 관리

## 에이전트 응답 지침
- 이 저장소와 관련한 에이전트의 모든 답변은 한글로만 작성합니다.
- 본 AGENTS.md의 지침을 우선 준수합니다.

## API 문서화 분리 원칙
- 모든 REST API는 컨트롤러와 문서(스웨거) 어노테이션을 분리합니다.
- 문서 전용 인터페이스를 `presentation/<feature>/docs/*ApiDocs.java`에 생성하고, 컨트롤러는 해당 인터페이스를 `implements` 합니다.
- Swagger/OpenAPI 어노테이션(`@Operation`, `@ApiResponses`, `@SecurityRequirement`, `@RequestBody` 등)은 인터페이스에만 작성합니다. 컨트롤러에는 비즈니스/보안 로직과 매핑만 둡니다.
- 예시
  - 인터페이스: `presentation/user/docs/UserApiDocs.java`
  - 컨트롤러: `presentation/user/UserController.java` (implements UserApiDocs)
