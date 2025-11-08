# PR: build(gitignore): Spring Boot application YAML 전부 무시

## 개요
- Spring Boot 환경설정 파일(`application.yml`, `application-*.yml`)을 버전관리에서 제외합니다.
- 민감정보(비밀키, DB/Redis 접속정보, 도메인 등) 유출 방지 및 환경별 분리를 위한 기본 작업입니다.

## 변경 사항
- .gitignore에 다음 규칙 추가
  - `src/main/resources/application.yml`
  - `src/main/resources/application-*.yml`

## 변경 이유(실무 포인트)
- 구성(secret)과 코드를 분리해 보안 리스크를 낮춤
- 로컬/스테이징/운영 간 설정 차이를 Git 이력에서 분리하여 배포 안정성 확보
- CI/CD에서 환경변수/시크릿 매니저 사용을 전제로 한 구조 정립

## 커밋 메시지 제안
```
build(gitignore): ignore Spring Boot application YAML files
```

## 브랜치 제안
- `feat/bootstrap-initial-setup`

## 검증 방법
1) 변경 후 Git이 설정 파일을 추적하지 않는지 확인
   - `git status`에 `src/main/resources/application*.yml`이 나타나지 않아야 함
2) 로컬 실행 시에는 환경변수/프로필로 설정 주입
   - 예) `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`

## 마이그레이션/주의 사항
- 이미 Git에 추적 중인 경우(스테이징/커밋됨), 아래 명령으로 인덱스에서 제거 필요(파일은 로컬에 유지)
```
# 스테이징되어 있거나 이미 추적 중인 파일을 인덱스에서만 제거
git rm --cached src/main/resources/application.yml || true
git rm --cached src/main/resources/application-local.yml || true
git rm --cached src/main/resources/application-prod.yml || true
```
- 이후 `.env` 혹은 CI 시크릿을 통해 `POSTGRES_*`, `REDIS_*`, `JWT_SECRET` 등 환경값을 주입하세요.
- 다음 단계에서 `application-example.yml`(샘플)을 문서용으로 추가하는 것을 권장합니다. (요청 시 별도 PR 제안)

## 관련 문서
- AGENTS.md: 보안 & 설정 팁(환경변수 목록)
