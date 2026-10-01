# 무신사 유사 브랜드/카테고리/상품 API

Spring Boot 기반의 브랜드·카테고리·상품 가격 조회 및 관리 API입니다. (무신사 "코디 완성" 백엔드 과제 형태)

## 기술 스택
- Java 21, Spring Boot 4.1.1
- Spring Web MVC, Spring Data JPA, Bean Validation
- H2 (인메모리), Lombok

## 실행
```bash
mvn spring-boot:run
```
- 애플리케이션 시작 시 `DataInitializer`가 브랜드 A~I, 8개 카테고리, 표준 가격표를 자동 적재합니다.
- H2 콘솔: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:musinsadb`, user: `sa`)

## 테스트
```bash
mvn test
```
`PriceQueryServiceTest`(조회 3종)와 `CrudServiceTest`(브랜드/상품 CRUD)로 로직을 검증합니다.
표준 데이터 기준 기대값: API1 총액 34,100 / API2 D 브랜드 36,100 / API3 상의 최저 C 10,000·최고 I 11,400.

## 도메인
- **Brand** — 브랜드 (name unique)
- **Category** — 카테고리 (상의, 아우터, 바지, 스니커즈, 가방, 모자, 양말, 액세서리)
- **Product** — 브랜드×카테고리 상품과 가격 (Brand N:1, Category N:1)

## API 명세

### 1. 카테고리별 최저가격 브랜드/가격/총액 조회
`GET /api/categories/lowest-prices`
```json
{
  "카테고리": [
    { "카테고리": "상의", "브랜드": "C", "가격": "10,000" },
    { "카테고리": "아우터", "브랜드": "E", "가격": "5,000" }
  ],
  "총액": "34,100"
}
```

### 2. 단일 브랜드로 전 카테고리 구매 시 최저 총액 브랜드 조회
`GET /api/brands/lowest-total`
```json
{
  "최저가": {
    "브랜드": "D",
    "카테고리": [ { "카테고리": "상의", "가격": "10,100" } ],
    "총액": "36,100"
  }
}
```

### 3. 카테고리 이름으로 최저/최고 가격 브랜드 조회
`GET /api/categories/{categoryName}/price-range` (예: `/api/categories/상의/price-range`)
```json
{
  "카테고리": "상의",
  "최저가": [ { "브랜드": "C", "가격": "10,000" } ],
  "최고가": [ { "브랜드": "I", "가격": "11,400" } ]
}
```
> 동일 최저가/최고가 브랜드가 여러 개면 배열로 모두 반환합니다.

### 4. 브랜드/상품 등록·수정·삭제 (CRUD)

**브랜드**
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/brands` | 목록 |
| GET | `/api/brands/{id}` | 단건 |
| POST | `/api/brands` | 등록 `{ "name": "J" }` |
| PUT | `/api/brands/{id}` | 수정 `{ "name": "J2" }` |
| DELETE | `/api/brands/{id}` | 삭제 (해당 브랜드 상품 함께 삭제) |

**상품**
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/products` | 목록 |
| GET | `/api/products/{id}` | 단건 |
| POST | `/api/products` | 등록 `{ "brandId": 1, "categoryName": "상의", "price": 10000 }` |
| PUT | `/api/products/{id}` | 수정 `{ "price": 9000, "categoryName": "바지" }` (categoryName 생략 가능) |
| DELETE | `/api/products/{id}` | 삭제 |

**카테고리 목록**: `GET /api/categories`

## 예외 처리
`GlobalExceptionHandler`가 공통 에러 응답을 반환합니다.
- 리소스 없음 → 404, 브랜드명 중복 → 409, 검증 실패/잘못된 요청 → 400

## 요청 예시 (curl)
```bash
curl http://localhost:8080/api/categories/lowest-prices
curl http://localhost:8080/api/brands/lowest-total
curl http://localhost:8080/api/categories/상의/price-range
curl -X POST http://localhost:8080/api/brands -H "Content-Type: application/json" -d '{"name":"J"}'
curl -X POST http://localhost:8080/api/products -H "Content-Type: application/json" -d '{"brandId":1,"categoryName":"상의","price":10000}'
```
