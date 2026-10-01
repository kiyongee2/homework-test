# 🏦 은행 계좌 관리 — 풀스택 (React + Spring Boot + H2)

프론트엔드(React)와 백엔드(Spring Boot)를 분리한 **풀스택** 프로젝트입니다.
브라우저의 React 앱이 REST API로 스프링부트 서버를 호출하고, 서버는 H2 DB에 데이터를 저장합니다.

```
[React + TS (Vite, :5173)]  --- fetch(/api/accounts) --->  [Spring Boot (:8080)]  ---JPA--->  [H2 DB]
```

## 폴더 구조
```
4-fullstack-react/
├─ backend/         Spring Boot + JPA + H2 (REST API)
│  └─ src/main/java/com/example/bank/
│     ├─ entity·repository·service·dto      (업무 로직 — 앞 예제와 동일)
│     ├─ controller/AccountController.java  (@RestController, /api/accounts)
│     ├─ controller/GlobalExceptionHandler.java
│     └─ config/CorsConfig.java             (프론트 :5173 허용)
└─ frontend/        React + TypeScript + Vite
   ├─ src/
   │  ├─ types.ts     (Account 타입)
   │  ├─ api.ts       (fetch 래퍼 — REST 호출)
   │  ├─ App.tsx      (화면 + 상태)
   │  └─ main.tsx, index.css
   ├─ vite.config.ts  (/api → :8080 프록시)
   └─ package.json
```

## 실행 방법 (터미널 2개)

### 1) 백엔드 — Spring Boot (포트 8080)
```bash
cd backend
mvn spring-boot:run
```
- IntelliJ에서 `BankApplication` 실행해도 됩니다.
- 뜨면 REST API: `http://localhost:8080/api/accounts`
- H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:bankdb`, 사용자 `sa`)
- 샘플 계좌 110-0001(홍길동), 110-0002(김철수) 자동 생성

### 2) 프론트엔드 — React (포트 5173)
```bash
cd frontend
npm install     # 최초 1회
npm run dev
```
- 브라우저에서 **http://localhost:5173** 접속 → 계좌 목록·개설·입금·출금·이체 화면

> **두 서버를 모두 켜 두어야** 합니다. 프론트(5173)의 `/api/*` 요청은 Vite 프록시가 백엔드(8080)로 전달합니다.

## 동작 원리 (프론트 ↔ 백엔드)
1. React가 `fetch('/api/accounts')` 호출
2. Vite 개발 서버가 이를 `http://localhost:8080/api/accounts`로 프록시 (그래서 개발 중 CORS 문제 없음)
3. 스프링부트 컨트롤러가 JSON 응답 → React가 화면에 렌더링
4. 입금·이체 등은 POST/PUT/DELETE로 호출 → 성공/실패 메시지를 화면에 표시

## REST API 요약
| 기능 | 메서드 | URL |
|------|--------|-----|
| 목록 | GET | `/api/accounts` |
| 조회 | GET | `/api/accounts/{no}` |
| 개설 | POST | `/api/accounts` |
| 입금 | POST | `/api/accounts/{no}/deposit` |
| 출금 | POST | `/api/accounts/{no}/withdraw` |
| 이체 | POST | `/api/accounts/transfer` |
| 해지 | DELETE | `/api/accounts/{no}` |

## 배포 시 (참고)
운영에서는 `npm run build`로 만든 `frontend/dist`를 정적 서버(nginx 등)나 스프링부트의 `static` 폴더에 올리고,
API 주소는 실제 백엔드 도메인으로 바꿉니다. (개발용 Vite 프록시는 개발 서버에서만 동작)
