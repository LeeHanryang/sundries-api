package io.github.leehanryang.sundriesapi.api.contoller;

import io.github.leehanryang.sundriesapi.surpport.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JWT 없이 접근 시 ApiException 검증
 * <p>
 * Given: 인증 토큰이 없는 상태
 * When: TODO 조회/목록/검색 요청
 * Then: 401 Unauthorized 및 MISSING_TOKEN 에러 응답
 */
class UnauthorizedAccessTest extends IntegrationTestSupport {

    @Test
    @DisplayName("Authorization 헤더 없이 Todo 상세 조회 요청 시 401과 에러 메시지 응답")
    void accessWithoutJwt() throws Exception {
        // When: JWT 없이 GET /todos/{id}
        mockMvc.perform(get("/todos/{id}", UUID.randomUUID()))
                // Then: 401 Unauthorized, MISSING_TOKEN 에러
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MISSING_TOKEN"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("Authorization 헤더 없이 Todo 목록 조회 요청 시 401과 에러 메시지 응답")
    void listWithoutJwt() throws Exception {
        // When: JWT 없이 GET /todos
        mockMvc.perform(get("/todos"))
                // Then: 401 Unauthorized, MISSING_TOKEN 에러
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MISSING_TOKEN"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("Authorization 헤더 없이 Todo 검색 요청 시 401과 에러 메시지 응답")
    void searchWithoutJwt() throws Exception {
        // When: JWT 없이 GET /todos/search?keyword
        mockMvc.perform(get("/todos/search").param("keyword", "test"))
                // Then: 401 Unauthorized, MISSING_TOKEN 에러
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MISSING_TOKEN"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
    }
}