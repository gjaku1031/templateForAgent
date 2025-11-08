#+ DDD Layered Architecture (현재 프로젝트 구조 설명)

이 문서는 현 구조가 DDD 레이어드 아키텍처에 어떻게 매핑되는지, 각 계층에 왜 해당 코드가 위치하는지, Swagger(OpenAPI) 문서화 전략까지 포함해 설명합니다.

## 전체 레이어 개요
- Presentation: HTTP API, 요청/응답 DTO, Swagger 문서
- Application: 유스케이스(트랜잭션), 도메인 서비스/리포지토리 조합, 예외 발생 지점
- Domain: 엔티티/값객체, 도메인 규칙, 리포지토리 인터페이스(+ Spring Data 인터페이스)
- Infrastructure: 영속성·외부 연동 구현물(단일 모듈에서는 Spring Data 규칙상 일부 구현을 domain 패키지에 둠)
- Cross‑cutting(Config/Error): JPA Auditing, QueryDSL, 글로벌 예외 처리, 에러 응답

폴더 매핑
- presentation: `.../presentation/**`
- application: `.../application/**`
- domain: `.../domain/**`
- cross-cutting: `.../common/**`

## Swagger(OpenAPI) 설계와 위치
- 문서 인터페이스: `.../presentation/{boundedContext}/docs/*ApiDocs.java`
  - Controller가 구현(implements)하는 인터페이스에 `@Tag`, `@Operation`, `@ApiResponses` 등을 부여합니다.
  - 이유
    - 컨트롤러를 얇게 유지(핵심: 라우팅 및 서비스 위임만 담당)
    - 문서 스펙을 인터페이스로 분리해 가독성/유지보수성 향상
    - Presentation 계층의 관심사(문서/표현)를 해당 계층 내부에 둠
- 라이브러리: springdoc-openapi-starter-webmvc-ui
  - 버전: 2.7.0 (Spring Boot 3.5.x / Spring Framework 6.2.x 호환)
  - UI 경로: `/swagger-ui.html`, 스펙 경로: `/v3/api-docs`

## Presentation 레이어
- 컨트롤러: `.../presentation/*/*Controller.java`
  - 역할: DTO 수신(검증), 서비스 호출, DTO 응답. 200대 응답만 반환.
  - 예외 처리는 하지 않으며 글로벌 핸들러에서 담당.
  - 왜 여기에?
    - 외부(HTTP)와 내부(Application) 사이의 경계. 표현 포맷(JSON, 페이징 DTO 등) 의존이 있으므로 Presentation 책임.
- DTO: `.../presentation/*/dto/*Request|*Response.java`
  - 요청/응답 전용 모델. 도메인 엔티티를 외부로 노출하지 않음.
  - Validation 어노테이션은 여기서 수행(Jakarta Validation).
- Docs: 위 Swagger 섹션 참고.

## Application 레이어
- 서비스: `.../application/*/*Service.java`
  - 역할: 유스케이스 조합, 트랜잭션 경계(`@Transactional`), 도메인/리포지토리 호출, 비즈니스 예외 발생.
  - 컨트롤러는 여기를 호출만 하고, 결과를 DTO로 매핑하거나 그대로 응답.
  - Lazy 로딩/트랜잭션 주의: Board 단건 조회는 서비스에서 DTO로 매핑해 LAZY 접근을 안전하게 처리.
  - 왜 여기에?
    - 도메인 규칙을 “사용”하여 작업을 완수하는 계층. I/O(웹/DB) 디테일로부터 상대적으로 독립.

## Domain 레이어
- 엔티티: `.../domain/*/*.java`
  - Setter 금지, 생성자(+빌더)로만 생성. 상태 변경은 도메인 메서드(`change(...)`)로 제한.
  - `BaseTimeEntity`로 생성/수정 시간 자동 관리(JPA Auditing).
- 리포지토리 인터페이스: `.../domain/*/*Repository.java`
  - 실용적 절충: Spring Data `JpaRepository`를 직접 상속해 개발 생산성 확보.
  - 커스텀 인터페이스/구현: Board의 페이징/검색은 QueryDSL 기반으로 분리.
- 커스텀 구현 클래스: `.../domain/board/BoardRepositoryImpl.java`
  - 이름 규칙: `BoardRepository` + `Impl` → Spring Data가 자동 조립.
  - 위치 이유: 단일 모듈에서 Spring Data 규칙(같은 패키지 네임스페이스) 하에 가장 간단히 동작. 엄격 DDD에선 멀티 모듈로 infra 분리가 적합함(아래 참고).

## Infrastructure 관점(단일 모듈 현실화)
- 엄격한 포트/어댑터 분리를 하려면
  - domain: 순수 리포지토리 인터페이스
  - infrastructure: Spring Data/JPA 어댑터 구현
  - → 단, 단일 모듈에서는 Spring Data의 구현 탐지 규칙 때문에 조합이 번거롭습니다. 실무 절충으로 커스텀 구현을 도메인 패키지에 둡니다.

## Cross‑cutting(설정/에러)
- 설정: `.../common/config/*`
  - `JpaConfig`: `@EnableJpaAuditing`로 생성/수정 일시 자동 주입
  - `QuerydslConfig`: `JPAQueryFactory` 빈 등록(필요 시 주입 사용)
  - `application.properties`: H2, JPA 설정(`open-in-view=false`, `ddl-auto=update`, SQL 포맷 등)
- 에러: `.../common/error/*`
  - `ErrorCode`(enum, Lombok @Getter), `ErrorResponse`(표준 에러 포맷)
  - 예외: `BusinessException`, `NotFoundException`
  - 글로벌 핸들러: `GlobalExceptionHandler`
    - 컨트롤러는 2xx만, 서비스는 예외를 던짐 → 핸들러가 400/404/409/500으로 매핑

## 페이징/검색(QueryDSL)
- Board 리스트: `BoardRepositoryCustom.search(keyword, pageable)` 구현체가 QueryDSL로 페이징 + 키워드 검색.
  - fetch-join으로 작성자 지연 로딩 최적화, `PageableExecutionUtils`로 카운트 효율화.
- User 리스트: 현재 Spring Data 기본 `findAll(pageable)` 사용(필요 시 QueryDSL로 통일 가능).

## 버전/도구
- JDK 17, Spring Boot 3.5.7, Spring Framework 6.2.x
- springdoc-openapi 2.7.0(부트 3.5.x 호환), QueryDSL 5 (jakarta)
- H2 인메모리 DB(개발용)

## 선택과 트레이드오프 요약
- Controller는 Service 호출만, 예외는 Global Handler에서 처리 → 표현/흐름과 오류 책임 분리
- 엔티티는 불변성 확립(Setter 금지), 도메인 메서드로만 상태 변경 → 도메인 규칙 캡슐화
- 단일 모듈에서 커스텀 Repo 구현은 domain 패키지에 둠 → Spring Data 규칙 충족(추후 멀티 모듈 전환 가능)
- Swagger는 Presentation에 인터페이스 분리 → 컨트롤러 가독성/문서 유지보수성 향상

## 향후 개선 아이디어
- 멀티 모듈로 엄격한 포트/어댑터 구조(domain/infrastructure 분리)
- User 리스트도 QueryDSL로 일관화
- 파라미터 변환/범위 오류(예: page/size 음수) 400 매핑 추가
- POST 응답을 201 Created + Location 헤더로 REST 표현 강화

