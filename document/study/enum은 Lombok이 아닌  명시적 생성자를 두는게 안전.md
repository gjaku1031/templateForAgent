# enum은 Lombok이 아닌 명시적 생성자를 두는 게 안전한 이유

## 요약
- enum의 생성자는 상수 초기화용이며 외부에서 호출되지 않습니다. Lombok로 대체할 실익이 거의 없고, 오히려 제약/호환 이슈를 초래할 수 있어 명시적 생성자를 유지하는 편이 안전합니다.
- Lombok은 enum에서 생성자 생성(@AllArgsConstructor/@RequiredArgsConstructor)을 지원하지 않거나 제한적으로 동작합니다. IDE/빌드 설정 차이로 경고 또는 예기치 않은 동작이 생길 수 있습니다.
- 따라서 enum에는 @Getter 정도만 사용하고, 생성자는 명시적으로 작성하는 것을 권장합니다.

## 상세 이유
- enum 생성자 제약
  - enum의 생성자는 암묵적으로 private/package-private이며 상수 정의 시에만 호출됩니다.
  - Lombok이 생성자를 만들어도 외부에서 호출되지 않으므로 이점이 거의 없습니다.

- 접근 제한자/시그니처의 명시성
  - 생성자 파라미터(순서/의미)를 코드로 드러내 API 의도를 분명히 합니다.
  - 일반 클래스에서 종종 발생하는 잘못된 접근 제한자(public 등) 노출 리스크를 구조적으로 차단합니다(enum은 본질적으로 외부 호출 불가).

- 안정성 및 도구 호환성
  - enum은 시스템 전역에서 널리 참조됩니다. 어노테이션 프로세서(Lombok) 의존을 줄이면 인크리멘탈 빌드나 IDE-Gradle 설정 미스매치에서 오는 문제를 예방할 수 있습니다.
  - Lombok은 enum에 대해 일부 어노테이션을 무시하거나 제한적으로 처리합니다(특히 생성자 관련). 명시적 코드가 가장 확실합니다.

- 인바리언트(불변식) 검증 용이
  - 생성자 내부에서 null 방지, 값 정규화, 파생 필드 계산 등의 로직을 명시적으로 넣을 수 있습니다.

- 프레임워크 관점
  - Spring/JPA 등은 enum의 생성자를 호출하지 않습니다(상수 정의 시점에만 사용). 생성자 자동 생성의 이점이 없습니다.
  - 대신 @Getter는 무해하고 유용하므로, 필드 접근용으로 Lombok을 사용하는 것은 권장됩니다.

## 권장 패턴 (예시)
```java
@Getter
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E4000", "Invalid request"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E4040", "Resource not found"),
    CONFLICT(HttpStatus.CONFLICT, "E4090", "Conflict occurred"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E5000", "Internal server error");

    private final HttpStatus status;
    private final String code;
    private final String message;

    // 명시적 생성자 유지
    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
```

## 비권장 패턴
- enum에 `@AllArgsConstructor` 등 Lombok 생성자 어노테이션 시도
  - Lombok이 생성자를 만들지 않거나, 도구/버전 차이에 따라 동작이 달라질 수 있습니다.
  - 가독성과 호환성을 해치며, 실질적 이점이 없습니다.

## 결론
- enum에는 @Getter만 Lombok을 적용하고, 생성자는 명시적으로 두는 것이 가장 안전합니다.
- 도메인의 핵심적인 enum은 보일러플레이트를 조금 감수하더라도 명시성과 호환성을 택하는 편이 장기 유지보수에 유리합니다.

