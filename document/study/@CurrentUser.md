# @CurrentUser 도입 배경과 설계 가이드

본 문서는 컨트롤러에서 현재 로그인 사용자의 식별자(userId 등)를 안전하고 느슨한 결합으로 주입하기 위해 `@CurrentUser`를 도입한 이유, 장단점, 대안(@AuthenticationPrincipal 메타 애노테이션)과 DDD 레이어 관점에서의 배치를 정리합니다.

## 왜 @CurrentUser 인가?
- 컨트롤러가 인프라 타입(CustomUserDetails 등)에 직접 의존하지 않도록 하기 위해서입니다.
- 필요한 값만(주로 `Long userId`) 주입받아 Presentation 계층을 단순화하고, 보안 구현 세부(Principal 구조)가 바뀌어도 컨트롤러 변경을 최소화합니다.

## 현재 구조(요약)
- 애노테이션: `presentation/common/annotation/CurrentUser.java`
- 리졸버: `infrastructure/web/CurrentUserArgumentResolver.java`
- 등록: `infrastructure/config/WebMvcConfig.java`
- 사용 예: 컨트롤러에서 `@CurrentUser Long userId`

```java
// Controller 예시
@PostMapping
@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
public ResponseEntity<Long> createBoard(@RequestBody @Valid BoardCreateRequest request,
                                        @CurrentUser Long userId) {
    Long id = boardService.create(userId, request.getTitle(), request.getContent());
    return ResponseEntity.ok(id);
}
```

## 동작 방식
- Spring MVC가 컨트롤러 메서드를 해석할 때, 등록된 `HandlerMethodArgumentResolver`(CurrentUserArgumentResolver)가
  - SecurityContext의 `Authentication#getPrincipal()`을 확인
  - principal이 `CustomUserDetails`인 경우 `user.id`를 추출해 `Long`에 주입
  - 인증되지 않았거나 타입이 다르면 `null`(해당 엔드포인트는 보통 `@PreAuthorize`로 보호)

```java
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
  public boolean supportsParameter(MethodParameter p) {
    return p.hasParameterAnnotation(CurrentUser.class) && Long.class.isAssignableFrom(p.getParameterType());
  }
  public Object resolveArgument(...) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) return null;
    Object principal = auth.getPrincipal();
    if (principal instanceof CustomUserDetails cud) {
      return cud.getUser().getId();
    }
    return null;
  }
}
```

## 장단점
- 장점
  - 컨트롤러가 인프라 세부 타입에 비의존(원시값/간단 DTO만 의존) → 레이어 경계가 깨끗함
  - Principal 내부 구조 변경 시 리졸버만 수정하면 됨(컨트롤러 안전)
  - JWT 클레임으로 전환 시에도 리졸버 로직만 교체하면 됨(추후 확장 용이)
- 단점
  - 리졸버/설정 코드가 필요함(WebMvcConfigurer에 등록)
  - 테스트(@WebMvcTest)에서 커스텀 리졸버 빈이 필요(보통 컴포넌트 스캔으로 자동 등록)

## 대안: @AuthenticationPrincipal 메타 애노테이션
메타 애노테이션 방식은 다음과 같습니다.

```java
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {}
```

이 경우 스프링 기본 `AuthenticationPrincipalArgumentResolver`를 그대로 사용합니다.

- 장점: 추가 리졸버/설정 불필요, 간단
- 단점: 기본적으로 컨트롤러 파라미터가 `CustomUserDetails` 등 인프라 타입과 결합됨
- 절충: expression 고정/전달로 원시값만 받기

```java
// 표현식 사용 예
public ResponseEntity<UserResponse> me(@AuthenticationPrincipal(expression = "user.id") Long userId) { ... }

// 메타 애노테이션+표현식을 함께 쓰고 싶다면 @AliasFor로 expression을 노출/고정하는 패턴도 가능
```

## DDD 레이어 관점 배치
- Presentation: `@CurrentUser` 애노테이션(웹 인터페이스 계약)
- Infrastructure: `CurrentUserArgumentResolver`(보안 구현 연동 – SecurityContext/Principal/JWT 등)
- Application: 비즈니스 유스케이스(AuthService, UserService 등), 컨트롤러에서 받은 `userId` 전달받아 처리
- Domain: 엔티티/도메인 서비스 – 보안 구현 세부와 무관

배치 이유
- 컨트롤러는 오로지 HTTP 계약과 최소 데이터만 알면 충분(표현/응답 책임)
- 인프라 세부는 인프라 레이어에서 캡슐화(보안 프레임워크/API 변경의 파급 최소화)

## JWT 클레임 기반으로 전환하기
현재 리졸버는 `CustomUserDetails`에서 ID를 추출합니다. JWT에 `userId` 클레임을 넣었다면 아래처럼 바꿀 수 있습니다.

- JwtFilter에서 `Authentication` 구성 시 `principal` 대신 `userId`만 들어있는 경량 Principal 사용
- 또는 Resolver가 직접 헤더의 토큰을 파싱해 `userId` 추출(보안/중복 우려로 권장도는 낮음)

권장 경로: 필터에서 `Authentication`을 만들 때 `principal`에 최소 정보(userId, username 등)만 넣어두고, 리졸버는 그 최소 정보만 사용.

## 테스트 팁
- @WebMvcTest 슬라이스: `@Import(WebMvcConfig.class)`로 리졸버 등록(컴포넌트 스캔이면 보통 불필요)
- `@WithMockUser` 사용 시 `SecurityContext`의 `principal`이 기본 문자열일 수 있으므로, 커스텀 `SecurityContextFactory`로 `CustomUserDetails`를 주입하거나, 리졸버가 문자열 principal일 때도 userId를 유추하도록 확장

## 선택 가이드
- “레이어 경계를 엄격히” + “장기 유지보수” → 현재 방식(ArgumentResolver) 추천
- “최대한 간단하게” + “프레임워크 기본 유지” → 메타 애노테이션(@AuthenticationPrincipal 별칭) + expression 추천

## 마이그레이션 체크리스트
- [x] @CurrentUser 애노테이션 추가(presentation)
- [x] CurrentUserArgumentResolver 구현/등록(infrastructure)
- [x] 컨트롤러에서 `@AuthenticationPrincipal` 제거 → `@CurrentUser Long userId` 도입
- [ ] 테스트 슬라이스에 리졸버 가시성 확인
- [ ] (선택) JWT 클레임으로 전환 시 리졸버 변경

