# Swagger/OpenAPI 보안 문서화 가이드 (Springdoc)

이 문서는 Springdoc(OpenAPI 3) 기반 Swagger UI에서 JWT 등 보안 스킴을 문서화하고, 엔드포인트에 인증 요구사항을 표현하는 방법을 정리한다. 레포지토리의 분리 원칙(컨트롤러 ↔ 문서 인터페이스)과 일관되게 사용하는 것을 목표로 한다.

## 1) 핵심 개념 요약
- 보안 스킴(Security Scheme): 어떤 인증 방식을 쓰는지 정의. 예) HTTP Bearer(JWT), Basic, OAuth2 등.
- 보안 요구(Security Requirement): 특정 API가 어떤 스킴으로 인증을 요구하는지 선언. Swagger UI에 자물쇠 아이콘과 Authorize 적용을 표시.
- Swagger UI 연동: 스킴을 등록하면 UI의 “Authorize” 버튼으로 토큰을 한 번 설정하고, 요구되는 엔드포인트 호출 시 자동으로 `Authorization` 헤더가 포함된다.

## 2) 스킴 등록 방법
- 전역 설정에서 스킴을 정의한다. 본 레포는 Bean 방식으로 등록한다.
- 파일: `src/main/java/com/gjaku1031/templateforagent/infrastructure/config/OpenApiConfig.java:16`

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("templateForAgent API").version("v1"))
        .components(new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"))
        );
}
```

- 필드 설명:
  - `type=HTTP`, `scheme=bearer`: HTTP Bearer 인증임을 표시.
  - `bearerFormat=JWT`: 토큰 포맷 힌트(표시용). 검증에는 영향 없음.
- 대안: 애노테이션 기반 등록도 가능하다(`@SecurityScheme(name="bearerAuth", type=HTTP, scheme="bearer", bearerFormat="JWT")`). 이 레포는 구성 분리를 위해 Bean 방식을 사용한다.

## 3) 엔드포인트에 인증 요구 연결
- 인증이 필요한 API에 `@SecurityRequirement(name = "bearerAuth")`를 추가한다.
- 분리 원칙상 컨트롤러가 아닌 “문서 전용 인터페이스”에 부착한다.
- 예시(게시판): `src/main/java/com/gjaku1031/templateforagent/presentation/board/docs/BoardApiDocs.java:25`

```java
@Operation(summary = "Create board (author=logged-in user)")
@SecurityRequirement(name = "bearerAuth")
ResponseEntity<Long> createBoard(...);
```

- 메서드 레벨 vs 인터페이스 레벨
  - 인터페이스 레벨에 두면 모든 엔드포인트가 인증 필요로 보이므로, 공개 API가 섞이면 혼동된다.
  - 이 레포는 공개/보호 API를 구분하기 위해 “인증이 필요한 메서드”에만 붙인다.
- 사용자 API처럼 전체가 보호되는 그룹은 인터페이스 레벨에 둘 수 있다.
  - 예: `src/main/java/com/gjaku1031/templateforagent/presentation/user/docs/UserApiDocs.java:21`

## 4) Authorization 헤더와 파라미터 숨김
- 보안 스킴을 등록하면 Swagger UI가 자동으로 `Authorization: Bearer <토큰>` 헤더를 추가한다.
- 따라서 헤더를 수동 파라미터로 문서화하지 말고, 필요 시 문서에서 숨긴다.
- 예시(유저 인증 문서): `src/main/java/com/gjaku1031/templateforagent/presentation/user/docs/UserAuthApiDocs.java:45`

```java
ResponseEntity<TokenResponse> refresh(@Parameter(hidden = true) String authorization, RefreshRequest body);
```

- 동일하게 서버 측에서 주입되는 `@CurrentUser Long userId`도 문서에서 숨긴다.
  - 예: `src/main/java/com/gjaku1031/templateforagent/presentation/user/docs/UserAuthApiDocs.java:61`

## 5) 다중 스킴/조합 규칙(참고)
- OpenAPI의 Security Requirement는 JSON 배열(OR)과 객체(AND) 조합으로 표현된다.
  - 같은 객체에 `{ schemeA: [], schemeB: [] }`를 함께 적으면 AND.
  - 배열에 객체를 여러 개 두면 OR. 예) `[ { bearerAuth: [] }, { basicAuth: [] } ]`.
- 대부분의 JWT 기반 API는 하나의 bearer 스킴만 사용한다.

## 6) 오류 응답 문서화 권장
- 인증/인가가 필요한 엔드포인트에는 아래 응답을 함께 명시하면 명확하다.
  - 401 Unauthorized: 액세스 토큰 누락/무효.
  - 403 Forbidden: 인증은 되었으나 권한 부족(예: ROLE/소유권 불일치).
- 예시(게시글 생성): `src/main/java/com/gjaku1031/templateforagent/presentation/board/docs/BoardApiDocs.java:34`

```java
@ApiResponses({
  @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
```

## 7) 이 레포에서의 권장 패턴(요약)
- 스킴 등록: OpenApiConfig Bean 방식으로 1회 정의(`bearerAuth`).
- 문서 위치: 컨트롤러가 아닌 `presentation/<feature>/docs/*ApiDocs.java`에만 Swagger 애노테이션 사용.
- 보호 API만 지정: 공개 API는 `@SecurityRequirement`를 붙이지 않는다. 보호 API만 메서드 레벨로 지정.
- 서버 주입 파라미터 숨김: `Authorization` 헤더, `@CurrentUser` 파라미터는 `@Parameter(hidden = true)`로 숨김.

## 8) 자주 하는 실수와 대응
- 컨트롤러에 Swagger 애노테이션을 직접 추가 → 문서/코드 뒤섞임. 인터페이스로 분리.
- 인터페이스 레벨에 보안 요구를 전역 선언 → 공개 API까지 인증 필요로 보임. 메서드 레벨로 이동.
- Authorization을 일반 헤더 파라미터로 문서화 → Swagger Authorize와 중복/혼란. 숨김 처리.
- 스킴 이름 불일치 → `@SecurityRequirement(name = "bearerAuth")`의 name은 스킴 등록 이름과 정확히 일치해야 함.

## 9) 빠른 체크리스트
- [ ] `OpenApiConfig`에 `bearerAuth` 스킴이 등록되어 있는가? (`OpenApiConfig.java:16`)
- [ ] 보호가 필요한 모든 엔드포인트에 `@SecurityRequirement(name = "bearerAuth")`가 있는가?
- [ ] 공개 엔드포인트에는 보안 요구가 붙어 있지 않은가?
- [ ] 서버 주입 파라미터(Authorization, @CurrentUser 등)는 문서에서 숨겼는가?
- [ ] 401/403 응답을 필요한 곳에 명시했는가?

