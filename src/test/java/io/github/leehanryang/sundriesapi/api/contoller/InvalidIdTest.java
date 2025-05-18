package io.github.leehanryang.sundriesapi.api.contoller;

import io.github.leehanryang.sundriesapi.domain.dto.LoginDTO;
import io.github.leehanryang.sundriesapi.domain.dto.SignUpDTO;
import io.github.leehanryang.sundriesapi.surpport.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 존재하지 않는 리소스 접근시 404 반환 검증
 * <p>
 * Given: 신규 사용자 가입 및 로그인으로 얻은 JWT
 * When: 랜덤 UUID 사용하여 GET /todos/{id} 요청
 * Then: 404 Not Found
 */
class InvalidIdTest extends IntegrationTestSupport {

    private String jwt;

    @BeforeEach
    void initUser() throws Exception {
        // Given: 회원가입 및 로그인
        String testUser = "testUser";
        String testPassword = "password";
        String testEmail = "tester@test.com";

        SignUpDTO signUp = SignUpDTO.builder()
                .email(testEmail)
                .username(testUser)
                .password(testPassword)
                .build();

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(signUp)))
                .andExpect(status().isCreated());

        LoginDTO login = new LoginDTO(null, null, testEmail, testPassword, null);
        String token = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String extractedToken = objectMapper.readTree(token).get("access_token").asText();
        jwt = "Bearer " + extractedToken;
    }

    @Test
    @Transactional
    @DisplayName("잘못된 Todo ID 요청 시 404 반환")
    void wrongTodoId() throws Exception {
        // When: 존재하지 않는 ID로 요청
        mockMvc.perform(get("/todos/{id}", UUID.randomUUID())
                        .header("Authorization", jwt))
                // Then: 404 Not Found
                .andExpect(status().isNotFound());
    }
}
