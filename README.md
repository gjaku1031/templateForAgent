# TemplateForAgent

이 프로젝트는 Spring Boot 기반의 예제 애플리케이션입니다. API 명세 문서는 SpringDoc OpenAPI를 통해 자동으로 생성되며, Swagger UI를 통해 손쉽게 확인할 수 있습니다.

## Swagger UI 사용법

1. 애플리케이션을 실행합니다.
2. 브라우저에서 [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html) 주소로 이동합니다.
3. 노출된 `/api/**` 엔드포인트에 대한 문서를 확인하고, "Try it out" 기능을 사용하여 API를 테스트할 수 있습니다.

OpenAPI 문서는 `/v3/api-docs` 및 `/v3/api-docs.yaml` 경로에서 JSON과 YAML 형식으로도 제공됩니다.

> ℹ️ **빌드 정보 표시**
>
> 애플리케이션이 `git.properties` 파일을 포함하고 있다면 Swagger UI의 문서 설명 영역에 최신 커밋 메시지, 커밋 ID, 작성자, 브랜치, 커밋 시간(한국 시간 기준)이 자동으로 표시됩니다.
