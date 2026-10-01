package com.spacecl.urlshortener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("단축 URL API 통합 테스트")
class UrlShortenerApiTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String shortenAndGetKey(String url) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + url + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").isString())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("key").asText();
    }

    @Test
    @DisplayName("POST /api/shorten 은 8글자 키와 단축 URL을 반환한다")
    void shortenReturns8CharKey() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://www.example.com/long/path\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com/long/path"))
                .andExpect(jsonPath("$.clickCount").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("key").asText()).hasSize(8);
        assertThat(json.get("shortUrl").asText()).endsWith("/" + json.get("key").asText());
    }

    @Test
    @DisplayName("GET /{key} 는 원본 URL로 302 리다이렉트한다 (요구사항 4)")
    void redirectsToOriginal() throws Exception {
        String key = shortenAndGetKey("https://www.example.com/redirect-target");

        mockMvc.perform(get("/" + key))
                .andExpect(status().isFound()) // 302
                .andExpect(header().string("Location", "https://www.example.com/redirect-target"));
    }

    @Test
    @DisplayName("리다이렉트할 때마다 클릭 수가 증가하고 통계 API로 확인된다 (요구사항 6)")
    void clickCountTrackedViaStatsApi() throws Exception {
        String key = shortenAndGetKey("https://www.example.com/track");

        mockMvc.perform(get("/" + key)).andExpect(status().isFound());
        mockMvc.perform(get("/" + key)).andExpect(status().isFound());

        mockMvc.perform(get("/api/stats/" + key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(key))
                .andExpect(jsonPath("$.clickCount").value(2));
    }

    @Test
    @DisplayName("같은 URL을 두 번 단축하면 서로 다른 키가 나오고 둘 다 동작한다 (요구사항 5)")
    void sameUrlGivesDifferentKeysBothWork() throws Exception {
        String url = "https://www.example.com/dup";
        String key1 = shortenAndGetKey(url);
        String key2 = shortenAndGetKey(url);

        assertThat(key1).isNotEqualTo(key2);

        mockMvc.perform(get("/" + key1))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", url));
        mockMvc.perform(get("/" + key2))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", url));
    }

    @Test
    @DisplayName("존재하지 않는 키로 통계를 조회하면 404를 반환한다")
    void statsForUnknownKeyReturns404() throws Exception {
        mockMvc.perform(get("/api/stats/zzzzzzzz"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("빈 URL로 단축을 요청하면 400을 반환한다")
    void shortenBlankUrlReturns400() throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/urls 는 생성된 단축 URL 목록을 반환한다")
    void listReturnsCreatedUrls() throws Exception {
        shortenAndGetKey("https://www.example.com/list-1");
        shortenAndGetKey("https://www.example.com/list-2");

        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }
}
