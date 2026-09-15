# Resolve Log

**작은 실천, 단단한 나.** 결심을 정하고, 하루하루 실천한 내용과 생각을 남기는 개인 일지입니다.

기존 [작심 프로젝트](https://github.com/jooyoungyun/jaksimProject)의 결심·실천 기록 흐름을 참고하여 **Nuxt + Vue + TypeScript / Java + Spring Boot + JPA + PostgreSQL**로 새로 구성했습니다. 소스, 테스트, IntelliJ 실행 설정, Docker 이미지 빌드 파일을 함께 제공합니다.

## 구현 기능

- 회원가입·로그인, BCrypt 비밀번호 해시, JWT 인증, 사용자별 데이터 분리
- 결심 생성·수정·보관·삭제, 카테고리·기간·반복 요일 설정
- 날짜별 일지: `해냈어요 / 조금 했어요 / 쉬어갔어요`, 메모, 실천 시간
- 오늘의 결심·달성률·최근 7일 완료 현황
- 월간 달력, 기간·결심·상태별 기록 조회, 페이지 이동
- 데스크톱·모바일에 대응하는 한국어 화면

같은 결심·날짜에는 한 건의 일지만 저장합니다. 미래 날짜에는 기록할 수 없으며, 보관한 결심의 기존 기록은 계속 조회·수정할 수 있습니다.

## 가장 빠른 실행: Docker

Docker Desktop 또는 Docker Engine + Compose v2를 실행한 뒤 **압축을 푼 `resolve-log` 폴더**에서 실행합니다. 이 방식은 PC에 Java나 Node.js를 별도로 설치하지 않아도 됩니다.

```sh
docker compose up -d --build --wait --wait-timeout 240
```

화면: **[http://localhost:3300](http://localhost:3300)** → `회원가입` → 첫 결심 만들기 → 오늘의 일지 작성.

첫 실행은 의존성과 이미지를 다운로드하므로 시간이 걸립니다. 기본 계정이나 예제 데이터는 자동 생성하지 않습니다. Windows에서는 `scripts/start.ps1`, macOS/Linux에서는 `sh scripts/start.sh`도 사용할 수 있습니다.

```sh
docker compose ps
docker compose logs -f backend
docker compose stop
```

PostgreSQL 데이터는 `resolve-log_postgres_data` 볼륨에 보관됩니다. `stop` 또는 `down` 후 다시 실행해도 유지됩니다. **`down -v`는 DB 데이터까지 지우므로 초기화할 때만 사용하세요.**

## IntelliJ에서 개발·디버깅

필요한 도구: **JDK 21, Node.js 24.11 이상(24.x), IntelliJ IDEA, Docker**. Maven은 `backend/mvnw`, `backend/mvnw.cmd`가 설치합니다.

1. 프로젝트 루트의 `pom.xml`을 IntelliJ에서 열고 Maven 프로젝트로 불러옵니다. Project SDK와 Maven JDK를 21로 설정합니다.
2. 루트에서 `docker compose up -d --wait postgres`로 DB만 시작합니다.
3. IntelliJ 실행 목록에서 **Resolve Log API (local)**을 선택하고 Debug를 누릅니다.
4. 별도 터미널에서 프런트 개발 서버를 실행합니다.

```sh
cd frontend
npm ci
npm run dev
```

5. [http://localhost:3300](http://localhost:3300)에서 결심·일지를 저장하면 Java 브레이크포인트로 진입합니다.

처음 읽을 상세 가이드: **[IntelliJ와 Docker 실행](docs/SETUP_KO.md)**. API 직접 호출 예제는 [requests.http](docs/requests.http)에 있습니다.

## 기술 구성

| 영역 | 구성 |
| --- | --- |
| 프런트 | Nuxt 4.5.2, Vue 3.5, TypeScript 5.9, Node.js 24 |
| API | Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA |
| DB | PostgreSQL 17, Flyway 버전별 스키마 관리 |
| 빌드 | npm lockfile, Maven Wrapper 3.9.11 |
| 테스트 | 실제 HTTP API 통합 테스트, Playwright 브라우저 테스트 |
| 실행 | IntelliJ 로컬 디버깅, Docker Compose, 컨테이너 원격 디버깅 |

인증 후 사용하는 개인 대시보드이므로 Nuxt는 클라이언트 렌더링으로 구성했습니다. Nuxt 서버가 `/api/**`를 Spring Boot로 전달합니다. 브라우저는 항상 접속한 웹 서버의 `/api` 주소를 사용합니다.

## 폴더 안내

| 경로 | 역할 |
| --- | --- |
| `frontend/app` | Vue 페이지·컴포넌트·인증 상태·스타일 |
| `frontend/server` | API 프록시와 웹 헬스 체크 |
| `frontend/tests/e2e` | 가입부터 기록 삭제까지 브라우저 테스트 |
| `backend/src/main/java/com/resolvelog` | 인증·결심·일지 API |
| `backend/src/main/resources/db/migration` | Flyway PostgreSQL 스키마 |
| `backend/src/test` | 인증·권한·동시성·일지 정책 통합 테스트 |
| `.run` | IntelliJ 로컬 및 Docker 원격 디버그 설정 |
| `scripts` | Windows PowerShell / macOS·Linux 실행·검증 스크립트 |
| `docs` | 설정, 설계, API 예제, 검증 결과 |

## 설정·검증

기본값으로 로컬 실행이 됩니다. 값을 바꾸려면 `.env.example`을 루트의 `.env`로 복사해 수정합니다. 기본 비밀번호와 JWT 키는 로컬 개발용입니다. 외부에 공개할 때는 키·비밀번호 교체와 HTTPS 설정이 필요합니다. Docker 포트는 기본적으로 로컬 PC에만 연결됩니다.

```sh
# backend 폴더: 테스트와 실행 JAR 생성 (Windows: .\mvnw.cmd verify)
./mvnw verify

# frontend 폴더: 타입 검사와 배포 빌드
npm run typecheck
npm run build
```

실제 PostgreSQL 통합 테스트는 루트에서 `sh scripts/test-postgres.sh` 또는 `./scripts/test-postgres.ps1`로 실행합니다. 일반 개발 DB와 분리된 일회용 DB를 5434 포트에 사용합니다.

전체 앱을 실행한 상태에서 프런트 폴더의 다음 명령으로 브라우저 테스트를 실행합니다. 테스트용 사용자가 한 명 생성됩니다.

```sh
npx playwright install chromium
npm run test:e2e
```

실제 확인한 범위와 실행 환경의 제약은 **[검증 결과](docs/VALIDATION.md)**, 데이터 정책·API 계약·원본 분석은 **[설계 문서](docs/ARCHITECTURE.md)**에 정리했습니다.
