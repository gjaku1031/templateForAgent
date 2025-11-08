# Spring Data JPA + QueryDSL 페이징 튜토리얼 (실무 패턴)

이 문서는 이 프로젝트에서 페이징 API를 구현하는 방법을 처음부터 끝까지 설명한다. 목표는 “컨트롤러를 최대한 얇게 유지”하고 “서비스에서 DTO 변환까지 마친 Page<DTO>를 반환하는” 2번 패턴을 정착시키는 것이다.

## 목표와 전제
- 전제: Spring Boot 3, Spring Data JPA, QueryDSL 5(Jakarta), Lombok 사용, `JPAQueryFactory` 빈 등록 완료.
- 목표: 검색/정렬/페이지네이션을 지원하는 목록 API 구현.
- 원칙: 컨트롤러는 입출력만, 서비스는 비즈니스 + DTO 변환, 레포지토리는 조회 최적화.

## 왜 2번 패턴(서비스에서 DTO 변환)인가
- 컨트롤러 얇게 유지: 컨트롤러는 “입력 받고 → 서비스 호출 → 결과 반환”만 담당.
- 테스트 용이: 서비스 단위 테스트에서 DTO 결과를 바로 검증 가능.
- 일관성: 본 레포에서 `BoardService.get()`처럼 DTO를 반환하는 메서드가 이미 존재. 목록도 동일 철학 적용.
- 프레젠테이션-도메인 결합 최소화: 엔티티 → DTO 변환은 서비스에서 책임지고, 컨트롤러는 DTO만 소비.

## 전체 흐름 개요
1) QueryDSL 설정(JPAQueryFactory Bean)
2) RepositoryCustom + Impl: 검색 쿼리 작성, Page<엔티티> 반환
3) Service: Page<엔티티>.map(Mapper)로 Page<DTO> 반환
4) Controller: 서비스 호출 결과(이미 DTO)를 그대로 반환 → 얇아짐
5) Swagger 문서: 엔드포인트 스펙은 문서 인터페이스에만 정의

---

## 1) QueryDSL 설정
- 위치: `src/main/java/com/gjaku1031/templateforagent/infrastructure/config/QuerydslConfig.java`
- JPAQueryFactory를 빈으로 등록해 어디서든 주입받아 사용.

```java
@Configuration
public class QuerydslConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }
}
```

Tip: 커스텀 레포지토리 구현에서는 이 빈을 생성자가 아니라 필드 주입(@RequiredArgsConstructor)으로 받아 사용한다.

---

## 2) 레포지토리: Custom + Impl (QueryDSL)
- 목적: 검색 조건과 정렬/페이징을 QueryDSL로 작성하고, Page<엔티티>를 반환.
- 인터페이스: `BoardRepositoryCustom`
- 구현체: `BoardRepositoryImpl` (이미 구현되어 있음)

예시(키워드 검색 + 작성자 fetch join, 최신순):

```java
public interface BoardRepositoryCustom {
    Page<Board> search(String keyword, Pageable pageable);
}
```

```java
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Board> search(String keyword, Pageable pageable) {
        List<Board> content = queryFactory
            .selectFrom(board)
            .leftJoin(board.author).fetchJoin()     // 다대일(fetch join) 페이징 안전
            .where(containsKeyword(keyword))        // null이면 조건 무시
            .orderBy(board.id.desc())               // 기본 정렬(예: 최신순)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        var countQuery = queryFactory
            .select(board.count())
            .from(board)
            .where(containsKeyword(keyword));

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchOne());
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null; // QueryDSL에서 null Predicate는 무시됨
        return board.title.containsIgnoreCase(keyword)
                .or(board.content.containsIgnoreCase(keyword));
    }
}
```

실무 포인트
- fetch join + 페이징: 다대일/일대일은 안전하지만, 일대다 컬렉션 fetch join은 카디널리티 증가로 페이징이 깨짐(지양).
- PageableExecutionUtils: count 쿼리를 지연/최적화하여 필요시만 수행.
- 동적 정렬: 간단한 경우엔 고정 정렬이 낫고, 필요 시 `orderBy(applySort(pageable))`로 확장(아래 확장 섹션 참고).

---

## 3) 서비스: Page<엔티티> → Page<DTO> 변환
- 서비스에서 DTO 변환까지 처리하여 컨트롤러를 얇게 만든다.
- 위치: `BoardService`

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;

    public Page<BoardResponse> search(String keyword, Pageable pageable) {
        return boardRepository.search(keyword, pageable)
                .map(BoardResponse::from); // Page.map 으로 페이지만 유지한 채 DTO 변환
    }
}
```

실무 포인트
- Page.map은 totalElements, pageable 메타데이터를 유지하면서 content만 변환.
- 서비스에서 변환하면 컨트롤러가 PageImpl를 직접 만들 필요가 없다.

---

## 4) 컨트롤러: 얇게 유지
- 컨트롤러는 서비스 호출 결과를 그대로 반환. 페이지 정보는 스프링이 자동 바인딩하거나, 명시적으로 PageRequest.of를 사용.

옵션 A) `page`, `size` 개별 파라미터
```java
@GetMapping
public ResponseEntity<Page<BoardResponse>> searchBoards(
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size) {
    var pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(boardService.search(keyword, pageable));
}
```

옵션 B) `Pageable` 통째로 바인딩(추천)
```java
@GetMapping
public ResponseEntity<Page<BoardResponse>> searchBoards(
        @RequestParam(name = "keyword", required = false) String keyword,
        @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(boardService.search(keyword, pageable));
}
```

실무 포인트
- 음수 page/과도한 size를 막고 싶으면 컨트롤러에서 한 번 정규화 또는 Validator로 제한.
- Swagger 문서 인터페이스에는 페이지 파라미터를 그대로 노출하되, 서버 주입 파라미터(@CurrentUser 등)는 숨김 처리.

---

## 5) Swagger 문서화 분리
- 위치: `presentation/board/docs/BoardApiDocs.java`
- 컨트롤러는 문서 애노테이션을 갖지 않고, 인터페이스에만 둔다.
- 보안이 필요한 엔드포인트는 `@SecurityRequirement(name = "bearerAuth")`를 메서드에 명시(공개 API와 혼동 방지).

예시(요약):
```java
@Operation(summary = "Search boards (paged)")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
  @ApiResponse(responseCode = "400", description = "Invalid request"),
  @ApiResponse(responseCode = "401", description = "Unauthorized")
})
ResponseEntity<Page<BoardResponse>> searchBoards(...);
```

---

## 확장: 동적 정렬 적용하기
- 프론트에서 `?sort=createdAt,desc&sort=title,asc` 같은 다중 정렬을 요청할 수 있다.
- QueryDSL Path로 매핑하는 헬퍼를 두고 적용.

```java
private OrderSpecifier<?>[] applySort(Pageable pageable) {
    List<OrderSpecifier<?>> orders = new ArrayList<>();
    for (Sort.Order o : pageable.getSort()) {
        boolean asc = o.getDirection().isAscending();
        switch (o.getProperty()) {
            case "id" -> orders.add(asc ? board.id.asc() : board.id.desc());
            case "createdAt" -> orders.add(asc ? board.createdAt.asc() : board.createdAt.desc());
            case "title" -> orders.add(asc ? board.title.asc() : board.title.desc());
            default -> { /* 알 수 없는 정렬 키는 무시 or 예외 */ }
        }
    }
    return orders.toArray(OrderSpecifier[]::new);
}
```

적용:
```java
.selectFrom(board)
.orderBy(applySort(pageable))
.offset(pageable.getOffset())
.limit(pageable.getPageSize())
.fetch();
```

---

## 성능/정확도 체크리스트
- [ ] 컬렉션(fetch join) + 페이징을 함께 사용하지 않는다(페이징 깨짐).
- [ ] count 쿼리는 꼭 필요한지 점검하고 `PageableExecutionUtils`로 지연.
- [ ] `keyword`가 null/blank일 때 조건을 생략(null Predicate 반환).
- [ ] 과도한 size 제한(예: max 100)과 기본 정렬을 안전하게 설정.
- [ ] N+1 경로에 필요한 부분만 fetch join(다대일/일대일)으로 최적화.

---

## 통합 예시(요약)
1) RepositoryImpl → `Page<Board>`
2) Service → `Page<BoardResponse>`
3) Controller → `ResponseEntity.ok(service.search(...))`

이 구조를 따르면 컨트롤러는 DTO Page를 그대로 반환할 뿐이며, 서비스는 도메인/프레젠테이션 경계를 아우르는 얇은 변환 레이어를 제공하게 된다. 실무에서 테스트/유지보수 편의성과 일관된 설계를 동시에 만족시킬 수 있는 패턴이다.

