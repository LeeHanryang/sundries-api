package io.github.leehanryang.sundriesapi.api.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.leehanryang.sundriesapi.domain.dto.LoginDTO;
import io.github.leehanryang.sundriesapi.domain.dto.SignUpDTO;
import io.github.leehanryang.sundriesapi.surpport.IntegrationTestSupport;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 회원 가입 → 로그인 → 프로필 조회 → 프로필 수정 → 회원 탈퇴 통합 흐름
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserFlowTest extends IntegrationTestSupport {

    // Test data
    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_EMAIL = "tester@test.com";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    private String jwt; // Bearer 토큰

    @BeforeEach
    @DisplayName("통합 시나리오 실행을 위한 회원 가입 & 로그인")
    @Transactional
    void setUp() throws Exception {
        // Given: 신규 회원가입 정보
        SignUpDTO signUp = SignUpDTO.builder()
                .email(TEST_EMAIL)
                .username(TEST_USER)
                .password(TEST_PASSWORD)
                .build();

        // When: POST /users/signup
        mvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(signUp)))
                // Then: 201 Created
                .andExpect(status().isCreated());

        // Given: 로그인 정보
        LoginDTO login = new LoginDTO(null, null, TEST_EMAIL, TEST_PASSWORD, null);

        // When: POST /users/login
        String body = mvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(login)))
                // Then: 200 OK, 액세스 토큰 획득
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract token
        String rawToken = om.readTree(body).get("access_token").asText();
        jwt = "Bearer " + rawToken;
    }

    @Test
    @Order(1)
    @Transactional
    @DisplayName("계정 조회")
    void getProfile() throws Exception {
        // Given: 유효한 JWT
        // When: GET /users/me
        mvc.perform(get("/users/me")
                        .header("Authorization", jwt))
                // Then: 200 OK, 사용자 정보 반환
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TEST_USER))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @Order(2)
    @Transactional
    @DisplayName("계정 수정")
    void updateProfile() throws Exception {
        // Given: 수정할 회원 정보
        SignUpDTO update = SignUpDTO.builder()
                .username("updatedUser")
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();

        // When: PUT /users/me
        mvc.perform(put("/users/me")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(update)))
                // Then: 200 OK, username 변경 확인
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedUser"));
    }

    @Test
    @Order(3)
    @Transactional
    @DisplayName("회원 탈퇴")
    void deleteAccount() throws Exception {
        // When: DELETE /users/me
        mvc.perform(delete("/users/me")
                        .header("Authorization", jwt))
                // Then: 204 No Content
                .andExpect(status().isNoContent());

        // When: 탈퇴 후 같은 토큰으로 GET 요청
        mvc.perform(get("/users/me")
                        .header("Authorization", jwt))
                // Then: 404 Not Found
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    @DisplayName("잘못된 프로필 수정 요청 시 400 반환")
    @Transactional
    void invalidUpdateProfile() throws Exception {
        // Given: 잘못된 DTO(빈 이메일)
        SignUpDTO invalid = SignUpDTO.builder()
                .username("user")
                .email("")
                .password("pw")
                .build();

        // When: PUT /users/me
        mvc.perform(put("/users/me")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(invalid)))
                // Then: 400 Bad Request
                .andExpect(status().isBadRequest());
    }
}