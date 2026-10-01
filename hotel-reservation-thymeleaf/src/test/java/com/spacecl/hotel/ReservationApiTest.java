package com.spacecl.hotel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spacecl.hotel.domain.Room;
import com.spacecl.hotel.domain.RoomType;
import com.spacecl.hotel.repository.ReservationRepository;
import com.spacecl.hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("예약 API 통합 테스트")
class ReservationApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Room room;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        roomRepository.deleteAll();
        room = roomRepository.save(new Room("201", RoomType.DOUBLE, 2));
    }

    private String in(int d) {
        return LocalDate.now().plusDays(d).toString();
    }

    private String body(Long roomId, int guests, int inDay, int outDay) {
        return "{"
                + "\"roomId\":" + roomId + ","
                + "\"guestName\":\"홍길동\","
                + "\"guestPhone\":\"010-1234-5678\","
                + "\"guestCount\":" + guests + ","
                + "\"checkInDate\":\"" + in(inDay) + "\","
                + "\"checkOutDate\":\"" + in(outDay) + "\"}";
    }

    private Long createReservation(int guests, int inDay, int outDay) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(room.getId(), guests, inDay, outDay)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/reservations 로 예약이 생성된다 (201)")
    void createReservation() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(room.getId(), 2, 1, 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.roomNumber").value("201"))
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.nights").value(2));
    }

    @Test
    @DisplayName("겹치는 기간 예약은 409를 반환한다")
    void overlappingReturns409() throws Exception {
        createReservation(2, 1, 5);
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(room.getId(), 2, 3, 7)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("존재하지 않는 객실 예약은 404를 반환한다")
    void unknownRoomReturns404() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(999999L, 2, 1, 3)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("잘못된 날짜(체크아웃<=체크인)는 400을 반환한다")
    void invalidDateReturns400() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(room.getId(), 2, 3, 3)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/rooms/available 은 예약된 객실을 제외한다")
    void availableExcludesReserved() throws Exception {
        // 다른 객실 하나 추가
        Room other = roomRepository.save(new Room("202", RoomType.TWIN, 2));
        createReservation(2, 1, 4); // room(201) 예약

        mockMvc.perform(get("/api/rooms/available")
                        .param("checkIn", in(2))
                        .param("checkOut", in(3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.roomNumber=='201')]").isEmpty())
                .andExpect(jsonPath("$[?(@.roomNumber=='202')]").isNotEmpty());
    }

    @Test
    @DisplayName("예약 취소 후에는 해당 기간 객실이 다시 예약 가능해진다")
    void cancelFreesRoom() throws Exception {
        Long id = createReservation(2, 1, 4);

        // 취소 전: 예약 불가 목록에서 201 제외됨
        mockMvc.perform(get("/api/rooms/available").param("checkIn", in(1)).param("checkOut", in(4)))
                .andExpect(jsonPath("$[?(@.roomNumber=='201')]").isEmpty());

        // 취소
        mockMvc.perform(post("/api/reservations/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // 취소 후: 다시 예약 가능
        mockMvc.perform(get("/api/rooms/available").param("checkIn", in(1)).param("checkOut", in(4)))
                .andExpect(jsonPath("$[?(@.roomNumber=='201')]").isNotEmpty());
    }

    @Test
    @DisplayName("투숙객 전화번호로 예약 목록을 조회한다")
    void listByGuestPhone() throws Exception {
        createReservation(2, 1, 3);
        createReservation(2, 5, 7);

        mockMvc.perform(get("/api/reservations").param("guestPhone", "010-1234-5678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
