package io.github.leehanryang.sundriesapi.api.contoller;

import io.github.leehanryang.sundriesapi.domain.dto.LoginDTO;
import io.github.leehanryang.sundriesapi.domain.dto.SignUpDTO;
import io.github.leehanryang.sundriesapi.surpport.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 회원가입 → 로그인 → JWT 토큰 발급 검증
 */
class AuthFlowTest extends IntegrationTestSupport {

    // Test data
    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_EMAIL = "tester@test.com";

    @Test
    @DisplayName("회원가입 후 로그인 시 JWT 토큰 발급")
    public void signUp_then_login_and_receive_jwt() throws Exception {
        // Given: 회원가입 정보 DTO
        SignUpDTO signUp = SignUpDTO.builder()
                .email(TEST_EMAIL)
                .username(TEST_USER)
                .password(TEST_PASSWORD)
                .build();

        // When: 회원가입 요청
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(signUp)))
                // Then: 201 Created, 반환된 이메일과 사용자명 검증
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.username").value(TEST_USER));

        // Given: 로그인 정보 DTO
        LoginDTO login = new LoginDTO(
                null, null, TEST_EMAIL, TEST_PASSWORD, null
        );

        // When: 로그인 요청
        String tokenResponse = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(login)))
                // Then: 200 OK, 액세스 토큰 존재
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // When: JSON 파싱 및 토큰 검증
        LoginDTO response = objectMapper.readValue(tokenResponse, LoginDTO.class);

        // Then: Bearer 형식의 JWT 토큰 검증
        String bearerToken = "Bearer " + response.getAccess_token();
        assertThat(bearerToken)
                .isNotBlank()
                .matches("^Bearer\\s+[\\w-]+\\.[\\w-]+\\.[\\w-]+$");
    }
}
