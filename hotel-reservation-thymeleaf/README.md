# 호텔 예약 시스템 (Thymeleaf UI)

호텔 투숙객 예약 기능을 **Thymeleaf 서버 사이드 렌더링 UI**로 제공하는 웹 애플리케이션입니다.
기존 `hotel-reservation`의 백엔드(도메인·서비스·리포지토리·REST API)를 그대로 재사용하고,
화면(웹 UI)을 추가한 버전입니다. **REST API와 웹 UI를 모두** 제공합니다.

## 기술 스택
- Java 21, Spring Boot 3.3.5
- Spring MVC + **Thymeleaf** (UI)
- Spring Data JPA + H2 (인메모리 DB)
- Bean Validation

## 실행 방법
```bash
cd hotel-reservation-thymeleaf
mvn spring-boot:run
```
브라우저에서 **http://localhost:8080** 접속 → 객실 조회 화면으로 이동합니다.
기동 시 샘플 객실 5개(101, 102, 201, 202, 301)가 자동 등록됩니다.

## 화면(UI) 구성
| 경로 | 화면 | 설명 |
|------|------|------|
| `GET /` | — | `/rooms`로 이동 |
| `GET /rooms` | 객실 조회 | 기간 검색 시 **예약 가능 객실만** 표시, 미검색 시 전체 객실 |
| `GET /reservations/new` | 예약 폼 | 객실/기간/투숙객 정보 입력 (객실·기간 자동 채움) |
| `POST /reservations` | (처리) | 예약 생성 → 상세로 이동. 중복/오류 시 폼에 메시지 표시 |
| `GET /reservations` | 예약 목록 | 전체 예약 표. 상세/취소 버튼 |
| `GET /reservations/{id}` | 예약 상세 | 예약 정보 + 취소 버튼 |
| `POST /reservations/{id}/cancel` | (처리) | 예약 취소 → 목록으로 |

### 사용 흐름
1. **객실 조회** 화면에서 체크인/체크아웃 날짜로 조회 → 예약 가능 객실 목록 확인
2. 원하는 객실의 **[예약하기]** 클릭 → 예약 폼(객실·기간 자동 입력)
3. 투숙객 정보 입력 후 **[예약 완료]**
4. **예약 목록**에서 상태 확인 및 **[취소]**

## REST API (그대로 유지)
UI와 별개로 API도 사용할 수 있습니다.
- `POST /api/reservations`, `GET /api/reservations/{id}`, `GET /api/reservations?guestPhone=`
- `POST /api/reservations/{id}/cancel`
- `GET /api/rooms`, `GET /api/rooms/available?checkIn=&checkOut=`

(API 상세 명세와 Postman 테스트는 기존 `hotel-reservation` 프로젝트의 `README.md`, `POSTMAN_TEST_GUIDE.md` 참고)

## 핵심 설계 (기존과 동일)
- **기간 중복 예약 방지**: `[checkIn, checkOut)` 반열린구간 겹침 판정(`Reservation.overlaps`). 체크아웃 당일 이어 입실은 허용.
- **동시성 처리**: 객실 행 비관적 락(`SELECT … FOR UPDATE`) + 트랜잭션으로 동시 예약 직렬화.
- **취소**: 상태만 `CANCELLED`로 변경(이력 보존), 취소분은 재예약 가능.
- **지연 로딩 대응**: 조회 시 `@EntityGraph(attributePaths = "room")`로 객실을 함께 로딩(open-in-view=false 환경에서 안전).

## 프로젝트 구조 (UI 관련 추가분)
```
src/main/java/com/spacecl/hotel/
├── controller/WebController.java      # Thymeleaf 화면 컨트롤러 (추가)
├── dto/ReservationForm.java           # 폼 바인딩 객체 (추가)
├── controller/{ReservationController, RoomController}.java  # REST API (유지)
├── domain/ service/ repository/ exception/ config/          # 백엔드 (재사용)

src/main/resources/
├── templates/
│   ├── fragments.html          # 공통 head/nav/alert 프래그먼트
│   ├── rooms.html              # 객실 조회
│   ├── reservation-form.html   # 예약 폼
│   ├── reservations.html       # 예약 목록
│   └── reservation-detail.html # 예약 상세
└── static/css/style.css        # 스타일
```

## 테스트
```bash
mvn test
```
서비스/도메인/REST API 테스트가 그대로 포함되어 있습니다.
