# URL 단축 서비스 (bit.ly 스타일)

긴 URL을 **8글자 키**의 짧은 링크로 바꿔 주는 단축 URL 서비스입니다.
Spring Boot 기반이며, **데이터베이스 없이 컬렉션(인메모리)** 으로만 데이터를 저장합니다.

## 기술 스택
- Java 21, Spring Boot 3.3.5 (spring-boot-starter-web, validation)
- 빌드: Maven
- 저장소: `ConcurrentHashMap` (인메모리, DB 미사용)
- 테스트: JUnit 5 + Spring MockMvc

## 실행 방법
```bash
# 프로젝트 루트에서
mvn spring-boot:run
```
실행 후 브라우저에서 http://localhost:8080 접속 → UI 페이지에서 바로 사용.

## 테스트 실행
```bash
mvn test
```

## 요구사항 대응표
| # | 요구사항 | 구현 |
|---|---------|------|
| 1 | bit.ly 같은 단축 URL 서비스 | `POST /api/shorten` + 리다이렉트 + UI |
| 2 | 키 8글자 | `KeyGenerator.KEY_LENGTH = 8` |
| 3 | 키 생성 알고리즘 자유 구현 | `SecureRandom` 기반 Base62(0-9a-zA-Z) 8자 |
| 4 | 단축 URL → 원본 리다이렉트 | `GET /{key}` → HTTP 302 |
| 5 | 같은 URL도 항상 새 단축 URL, 기존 것도 유효 | 매 요청마다 새 키 생성, 저장소에 모두 보존 |
| 6 | 리다이렉트마다 카운트 증가 + 조회 API | `AtomicLong` 카운트 + `GET /api/stats/{key}` |
| 7 | DB 없이 컬렉션 저장 | `UrlRepository` = `ConcurrentHashMap` |
| 8 | 테스트 코드 | 서비스 단위 + API 통합 테스트 |
| 9 | UI 페이지 | `src/main/resources/static/index.html` |

## API 명세

### 1. URL 단축
```
POST /api/shorten
Content-Type: application/json

{ "url": "https://www.example.com/very/long/path" }
```
응답 `201 Created`
```json
{
  "key": "3onGWak1",
  "shortUrl": "http://localhost:8080/3onGWak1",
  "originalUrl": "https://www.example.com/very/long/path",
  "clickCount": 0,
  "createdAt": "2026-09-08T05:00:00Z"
}
```
> 스킴(`http://`/`https://`)이 없으면 `https://`가 자동으로 붙습니다.

### 2. 단축 URL 리다이렉트
```
GET /{key}
```
→ `302 Found`, `Location: <원본 URL>`. 호출될 때마다 클릭 수가 1 증가합니다.

### 3. 통계 조회
```
GET /api/stats/{key}
```
응답 `200 OK` — 해당 키의 원본 URL, 클릭 수, 생성 시각. (조회는 카운트를 올리지 않음)

### 4. 전체 목록 (UI용)
```
GET /api/urls
```
생성된 모든 단축 URL을 최근순으로 반환합니다.

## 예시 (curl)
```bash
# 단축
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.example.com/hello"}'

# 리다이렉트 (키는 위 응답값 사용)
curl -i http://localhost:8080/3onGWak1

# 통계
curl http://localhost:8080/api/stats/3onGWak1
```

## 프로젝트 구조
```
src/main/java/com/spacecl/urlshortener/
├── UrlShortenerApplication.java      # 진입점
├── controller/
│   ├── UrlApiController.java         # 단축/통계/목록 REST API
│   └── RedirectController.java       # /{key} → 302 리다이렉트
├── service/
│   ├── UrlShortenerService.java      # 핵심 비즈니스 로직
│   └── KeyGenerator.java             # 8자 Base62 키 생성
├── repository/
│   └── UrlRepository.java            # ConcurrentHashMap 저장소 (DB 미사용)
├── model/
│   └── ShortUrl.java                 # 도메인 모델 (AtomicLong 카운트)
├── dto/
│   ├── ShortenRequest.java
│   └── UrlResponse.java
└── exception/
    ├── UrlNotFoundException.java
    └── GlobalExceptionHandler.java   # 404/400 JSON 오류 응답

src/main/resources/static/index.html # UI 페이지
src/test/java/...                     # 단위 + 통합 테스트
```

## 설계 노트
- **키 생성**: `SecureRandom`으로 Base62 8자를 무작위 추출. 키 공간 62^8 ≈ 2.18×10¹⁴로 충돌 확률이 매우 낮으며, 저장 시 `putIfAbsent`로 충돌을 원자적으로 재확인해 유일성을 보장합니다.
- **항상 새 키(요구사항 5)**: 원본 URL로 역인덱스를 두지 않고 매 요청마다 새 키를 발급하므로, 같은 URL이라도 매번 다른 단축 URL이 생기고 기존 키도 그대로 보존됩니다.
- **동시성**: 저장소는 `ConcurrentHashMap`, 클릭 수는 `AtomicLong`으로 관리해 동시 리다이렉트에도 카운트가 정확합니다.
