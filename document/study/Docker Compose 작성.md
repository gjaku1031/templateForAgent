# templateForAgent 개발용 인프라 구성 파일입니다.
## 목적: 로컬 개발 환경에서 PostgreSQL(DB)과 Redis(캐시)를 손쉽게 실행/중지합니다.

## 사용법(기본):
###  1) 이 파일이 있는 폴더에서(Optional) .env 파일을 생성하고 변수 값을 설정합니다.
 - 샘플: ./.env.example 참고 → `cp .env.example .env`
 - docker compose는 같은 디렉토리의 .env를 자동으로 읽습니다.

###  2) 컨테이너 실행:   
```shell
docker compose up -d
```

###  3) 컨테이너 중지:   
```shell
docker compose down
```

###  4) 데이터까지 삭제: 
```shell
docker compose down -v
```


## 애플리케이션 연결 정보:
 - 로컬에서 앱(스프링)을 직접 실행할 때

```
JDBC_URL=jdbc:postgresql://localhost:5432/templateforagent
Redis 호스트/포트: localhost:6379
```
- 앱도 컨테이너에서 실행할 때(동일 compose 네트워크)

```
JDBC_URL=jdbc:postgresql://postgres:5432/templateforagent   # 서비스 이름을 호스트로 사용
Redis 호스트/포트: redis:6379
변수 치환 규칙: ${VAR:-default} → .env 또는 쉘 환경변수 VAR이 없으면 default 사용
```