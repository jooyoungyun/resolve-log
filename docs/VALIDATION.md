# 검증 결과

검증일: 2026-09-15. 아래는 프로젝트를 생성한 환경에서 실제 수행한 결과입니다.

| 항목 | 결과 | 범위 |
| --- | --- | --- |
| Java 21 컴파일·실행 JAR 패키징 | 통과 | Spring Boot 4.1.1, Maven 3.9.11 |
| API 통합 테스트 | **10개 통과, 실패 0, 오류 0** | Spring Boot 실제 HTTP 서버 + H2 PostgreSQL 호환 모드 |
| Flyway 초기 스키마·JPA validate | 통과 | 위 테스트 DB에서 확인 |
| Nuxt TypeScript 검사 | 통과 | `npm run typecheck` |
| Nuxt 프로덕션 빌드·서버 기동 | 통과 | `npm run build`, 생성된 Nitro 서버 실행 |
| Chromium 브라우저 통합 시나리오 | **1개 통과** | 실제 Nuxt → Spring Boot → 테스트 DB 연동 |
| 모바일 너비 검사 | 통과 | 390 × 844, 가로 넘침 없음 |
| Compose 기본·디버그·테스트 구성 | 통과 | Compose v2.39.4 `config --quiet` |
| POSIX 셸 스크립트 문법 | 통과 | start, db-up, test-postgres, Maven Wrapper |
| Docker 이미지 실제 빌드·전체 기동 | 미실행 | 이 환경에 Docker 엔진 없음 |
| 실제 PostgreSQL 17 통합 테스트 | 미실행 | Docker 엔진이 있는 PC에서 제공 스크립트 실행 필요 |
| IntelliJ GUI 브레이크포인트 연결 | 미실행 | 실행·원격 디버그 설정 및 수동 절차 제공 |

H2 호환 모드 검증은 실제 PostgreSQL의 동시성·타입·마이그레이션 동작을 완전히 대체하지 않습니다. 처음 사용할 PC에서 아래 PostgreSQL 검증을 실행한 뒤 개발을 시작하는 것을 권장합니다.

## API 통합 테스트가 확인한 동작

테스트 파일: `backend/src/test/java/com/resolvelog/ApiIntegrationTest.java`.

1. 회원가입·로그인, 중복 이메일·비밀번호·입력 검증
2. 미인증 요청 거부와 다른 사용자 데이터 접근 차단
3. 잘못된 결심 기간·요일 거부
4. 같은 날짜 일지 갱신과 대시보드 집계
5. 미래 날짜·예정되지 않은 날짜 기록 거부
6. 결심 보관 후 기존 기록 보존·수정 및 새 기록 거부
7. 동시 저장 요청 후 DB에 일지 한 건만 존재
8. 오래된 결심 버전 및 기존 일지를 제외하는 기간 변경 거부
9. 결심 삭제 시 관련 일지 삭제, 없는 일지 삭제의 멱등성
10. 월간 달력·일지 페이지·조회 기간 경계

기본 검증 명령은 `backend` 폴더에서 `./mvnw verify` 또는 `./mvnw.cmd verify`입니다. 기본 테스트는 H2를 사용하며 별도 DB가 필요 없습니다. H2는 test scope이므로 실제 서비스 JAR에 포함되지 않습니다.

## 실제 PostgreSQL에서 동일한 테스트 실행

JDK 21과 실행 중인 Docker 엔진이 필요합니다. 프로젝트 루트에서 실행합니다.

Windows PowerShell:

```powershell
./scripts/test-postgres.ps1
```

macOS / Linux:

```sh
sh scripts/test-postgres.sh
```

테스트 DB는 개발 DB와 별개이며 5434 포트·`resolve_log_test` DB를 사용합니다. 데이터는 tmpfs에 저장하고 테스트가 끝나면 컨테이너를 정지합니다. 테스트 실패 시 명령이 실패 상태로 종료됩니다.

## 브라우저 시나리오

테스트 파일: `frontend/tests/e2e/journal.spec.ts`.

회원가입 → 첫 결심 생성 → 오늘 일지·시간 저장 → 기록 보관함 조회 → 수행 상태·메모·시간 수정 → 새로고침 후 유지 확인 → 모바일 너비 확인 → 결심 삭제와 일지 삭제 확인 → 로그아웃. 브라우저의 실행 오류도 확인합니다.

전체 앱을 먼저 실행한 뒤 프런트 폴더에서 실행합니다.

```sh
npm ci
npx playwright install chromium
npm run test:e2e
```

테스트는 고유한 이메일로 사용자를 생성합니다. 생성한 결심·일지는 시나리오 마지막에 삭제하며 테스트 사용자는 남습니다. 기본 대상은 `http://localhost:3300`, 다른 주소는 `E2E_BASE_URL` 환경 변수로 지정할 수 있습니다. 생성 환경에서는 브라우저 다운로드 경로의 네트워크 제약 때문에 별도로 준비한 Chromium 153 바이너리를 `PLAYWRIGHT_CHROMIUM_EXECUTABLE`로 지정하여 동일한 테스트를 수행했습니다.

## 화면 캡처

`docs/screenshots`의 화면은 실제 빌드된 프런트와 API에 테스트 사용자·예제 기록을 생성한 뒤 촬영한 것입니다. 배포 프로젝트의 DB에 예제 데이터를 넣는 초기화 로직은 없습니다.

- `dashboard-desktop.png`: 일별 결심·달성률·7일 현황
- `journal-desktop.png`: 월간 달력과 기록 보관함
- `goal-form-desktop.png`: 결심 작성
- `dashboard-mobile.png`: 모바일 화면

## 포함한 파일과 제외한 파일

압축파일에는 소스·lockfile·Maven Wrapper·Dockerfile·Compose·테스트·문서·화면 캡처를 넣었습니다. `node_modules`, Maven `target`, Nuxt 빌드 산출물, 실제 `.env`, 테스트 계정 데이터, 개발 환경에 임시로 설치한 JDK·브라우저는 넣지 않았습니다. 내려받은 뒤 문서의 명령으로 의존성과 이미지를 재생성합니다.
