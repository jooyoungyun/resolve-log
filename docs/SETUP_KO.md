# IntelliJ 개발·디버깅 및 Docker 실행

## 1. 개발 도구와 포트

| 도구 | 사용 버전·설정 |
| --- | --- |
| Java | JDK 21; IntelliJ Project SDK와 Maven Runner JDK 모두 21 |
| Node.js | 24.11 이상인 24.x; npm 포함 |
| Maven | `backend/mvnw` 또는 `backend/mvnw.cmd`로 3.9.11 사용 |
| IDE | IntelliJ IDEA; Java Application 실행은 Spring 전용 기능 없이도 가능 |
| 컨테이너 | 실행 중인 Docker Desktop 또는 Engine, Compose v2 |

[Spring Boot 4.1의 공식 실행 요구사항](https://docs.spring.io/spring-boot/system-requirements.html)을 기준으로 Java 21을 선택했습니다.

| 주소·포트 | 용도 |
| --- | --- |
| `http://localhost:3300` | Nuxt 화면·브라우저 API 요청 |
| `http://localhost:8080` | Spring Boot 직접 호출 |
| `localhost:5433` | 개발 PostgreSQL (컨테이너 내부는 5432) |
| `localhost:5005` | 선택적으로 켜는 컨테이너 JVM 디버그 |
| `localhost:5434` | PostgreSQL 테스트 전용 DB |

## 2. 로컬 개발 준비

압축을 풀고 `resolve-log` 폴더로 이동합니다. 기본 설정으로 바로 개발할 수 있습니다. 수정할 값이 있다면 환경 파일을 복사합니다.

PowerShell:

```powershell
Copy-Item .env.example .env
```

macOS / Linux:

```sh
cp .env.example .env
```

기존 `.env`가 있다면 덮어쓰지 말고 필요한 값만 수정하세요. Spring Boot는 로컬 실행 시 루트 `.env`를 읽습니다. Docker Compose도 같은 파일을 읽습니다. 환경 파일에는 단순한 `KEY=value` 형태를 사용합니다.

DB만 시작합니다.

```sh
docker compose up -d --wait postgres
```

최초 기본 접속 정보는 DB `resolve_log`, 사용자 `resolve_log`, 비밀번호 `resolve_log_local`입니다. 앱 실행 시 Flyway가 테이블을 생성하고, JPA는 스키마를 검증합니다. 수동으로 테이블을 만들 필요가 없습니다.

## 3. IntelliJ에서 API 디버깅

1. `File → Open`에서 루트 `pom.xml`을 선택하고 Maven 프로젝트로 엽니다. Maven 창에서 `Resolve Log`와 `Resolve Log API`가 보이는지 확인합니다.
2. `File → Project Structure → Project SDK`를 **JDK 21**로 설정합니다. `Settings → Build Tools → Maven`의 Importer/Runner JDK도 21을 사용합니다.
3. Maven 새로고침이 끝날 때까지 기다립니다.
4. 공유 실행 설정 **Resolve Log API (local)**을 선택합니다.
5. `backend/src/main/java/com/resolvelog/journal/JournalService.java`의 `save()` 안에 브레이크포인트를 설정하고 **Debug**를 누릅니다.
6. `Tomcat started on port 8080` 로그와 [API 상태](http://localhost:8080/actuator/health)의 `{"status":"UP"}`를 확인합니다.

공유 설정이 나타나지 않거나 모듈 이름을 다르게 가져온 경우에는 다음 값으로 `Run → Edit Configurations → Application`을 생성합니다.

| 필드 | 값 |
| --- | --- |
| Name | Resolve Log API (local) |
| Main class | `com.resolvelog.ResolveLogApplication` |
| Use classpath of module | `resolve-log-api`의 main 모듈 |
| JRE | JDK 21 |
| VM options | `-Dspring.profiles.active=local` |
| Working directory | 프로젝트의 `backend` 폴더 |

## 4. Nuxt 개발 서버

IntelliJ 터미널 또는 별도 터미널에서 다음을 실행합니다.

```sh
cd frontend
npm ci
npm run dev
```

[화면 열기](http://localhost:3300) → 회원가입 → 결심 생성 → `기록하기` → `일지 저장`을 실행하면 Java 브레이크포인트에서 멈춥니다. 브레이크포인트를 잡은 동안 프런트 요청이 15초 후 시간 초과될 수 있습니다. JVM을 계속 실행한 뒤 화면을 새로고침하면 저장 결과를 확인할 수 있습니다.

Vue 파일은 HMR로 반영됩니다. Java 코드 변경은 IntelliJ의 재빌드/HotSwap 범위에 따라 반영되며, 클래스 구조나 설정 변경은 API를 재시작합니다. Vue·TypeScript 편집 지원 범위는 IntelliJ 설치 버전과 활성 플러그인에 따라 다릅니다.

API만 터미널에서 실행하려면 `backend` 폴더에서 다음을 실행합니다.

```sh
./mvnw spring-boot:run
```

Windows에서는 `./mvnw.cmd spring-boot:run`을 사용합니다.

## 5. 이미지 빌드와 전체 실행

IntelliJ API와 Nuxt 개발 서버를 먼저 종료해 8080·3300 포트를 비웁니다. 기존 개발 DB는 같은 볼륨을 사용합니다. 프로젝트 루트에서 실행합니다.

```sh
docker compose up -d --build --wait --wait-timeout 240
```

이 명령은 API·웹 이미지를 빌드하고 DB → API → 웹 순서로 헬스 체크를 기다립니다. [Compose의 `up`, `--build`, `--wait` 설명](https://docs.docker.com/reference/cli/docker/compose/up/)을 참고하세요.

| 서비스 | 이미지 | 빌드 동작 |
| --- | --- | --- |
| postgres | `postgres:17-alpine` | 공식 DB 이미지 |
| backend | `resolve-log-api:1.0.0` | JDK 21로 테스트·JAR 생성, JRE 21로 실행 |
| frontend | `resolve-log-web:1.0.0` | npm ci → 타입 검사 → Nuxt 빌드, Node 24로 실행 |

다단계 빌드로 실행 이미지에 빌드 도구·소스·테스트 의존성을 넣지 않습니다. API와 웹은 일반 사용자로 실행합니다. API의 H2 의존성은 테스트 범위여서 실행 JAR에 포함되지 않습니다.

```sh
# 상태와 로그
docker compose ps
docker compose logs -f backend frontend

# 코드 수정 후 재빌드
docker compose up -d --build --wait

# 종료; 데이터 유지
docker compose down
```

다시 로컬 IntelliJ 개발로 전환할 때는 `docker compose stop frontend backend`를 실행하고 API Debug와 `npm run dev`를 시작합니다.

## 6. Docker API에 IntelliJ 연결

컨테이너 JVM 디버그가 필요할 때만 추가 Compose 파일을 사용합니다.

```sh
docker compose -f compose.yaml -f compose.debug.yaml up -d --build --wait
```

IntelliJ의 **Resolve Log Docker Debug** 설정을 선택하고 Debug를 누릅니다. `localhost:5005`에 연결됩니다. 별도로 생성하려면 `Remote JVM Debug`, attach, socket, host `localhost`, port `5005`를 선택합니다. [JetBrains 원격 JVM 디버깅 안내](https://www.jetbrains.com/help/idea/tutorial-remote-debug.html).

디버그 중인 소스는 이미지에 빌드된 소스와 같아야 합니다. 소스가 달라졌으면 이미지를 재빌드하고 다시 연결합니다. 5005는 로컬 PC에만 노출되며 기본 실행에는 포함되지 않습니다.

일반 모드로 되돌리려면 다음을 실행합니다.

```sh
docker compose up -d --force-recreate backend
```

## 7. 환경 설정 변경

| 변수 | 기본값·의미 |
| --- | --- |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | DB 이름·사용자·비밀번호 |
| `POSTGRES_PORT` | 호스트 DB 포트 5433 |
| `DB_URL` | 로컬 IntelliJ/API용 JDBC URL; DB 포트·이름 변경 시 함께 수정 |
| `BACKEND_PORT` | 호스트 API 포트 8080; Docker 내부는 항상 8080 |
| `FRONTEND_PORT` | Docker 웹 호스트 포트 3300 |
| `FRONTEND_ORIGIN` | 허용할 브라우저 origin; 기본 `http://localhost:3300` |
| `APP_TIMEZONE` | 업무 날짜 기준, 기본 `Asia/Seoul` |
| `JWT_SECRET` | 최소 32 UTF-8 바이트의 서명 키 |

Nuxt의 개발 서버는 루트 `.env`를 자동으로 읽지 않습니다. API 포트·시간대를 변경했다면 프런트 터미널에서 아래 서버 환경 변수를 함께 설정합니다. 기본값 사용 시에는 설정할 필요가 없습니다.

PowerShell 예:

```powershell
$env:NUXT_API_BASE = "http://127.0.0.1:8081"
$env:NUXT_PUBLIC_TIMEZONE = "Asia/Seoul"
npm run dev -- --port 3301
```

macOS / Linux 예:

```sh
NUXT_API_BASE=http://127.0.0.1:8081 NUXT_PUBLIC_TIMEZONE=Asia/Seoul npm run dev -- --port 3301
```

DB가 이미 만들어진 뒤 `POSTGRES_PASSWORD`만 수정하면 기존 DB 사용자 비밀번호는 자동 변경되지 않습니다. 기존 자격 증명을 사용하거나 DB에서 비밀번호를 변경한 후 설정을 맞추세요. 데이터를 보존해야 한다면 볼륨을 지워서 해결하지 마세요.

## 8. 문제 해결과 백업

| 현상 | 확인할 내용 |
| --- | --- |
| `port is already allocated` | 같은 포트의 IntelliJ API·Nuxt·다른 컨테이너 종료 또는 포트 변경 |
| `Connection refused` / DB 연결 실패 | `docker compose ps postgres`, JDBC URL, 5433/5432 구분 |
| Docker 명령 실패 | Docker 엔진 실행 여부, `docker compose version` |
| `release version 21 not supported` | `java -version`, `./mvnw -v`, IntelliJ의 Maven JDK를 21로 설정 |
| 로그인 만료 | 토큰은 기본 8시간 유효; 다시 로그인 |
| API 연결 오류 | `docker compose logs backend frontend`, 또는 로컬 8080 API 실행 여부 |
| 스크립트 실행 권한 없음 | macOS/Linux는 `sh scripts/start.sh`; Maven은 `chmod +x backend/mvnw` |
| PowerShell 스크립트 정책 | README의 동일한 `docker compose` 명령을 직접 실행 |

기본 DB 설정의 SQL 백업 예입니다. 컨테이너 안에서 파일을 만든 뒤 복사하므로 PowerShell 출력 인코딩에 영향을 받지 않습니다.

```sh
docker compose exec postgres pg_dump -U resolve_log -d resolve_log -f /tmp/resolve-log-backup.sql
docker compose cp postgres:/tmp/resolve-log-backup.sql ./resolve-log-backup.sql
```

외부 서비스로 운영하는 단계에서는 별도 도메인·HTTPS, 운영 비밀값, 백업 정책, 로그인 요청 제한을 구성하세요. 현재 납품 범위는 로컬 개발과 Docker 일괄 실행이 가능한 개인 일지 애플리케이션입니다.
