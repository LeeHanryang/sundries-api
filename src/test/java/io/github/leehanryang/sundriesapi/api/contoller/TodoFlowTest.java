package io.github.leehanryang.sundriesapi.api.contoller;

import io.github.leehanryang.sundriesapi.domain.dto.LoginDTO;
import io.github.leehanryang.sundriesapi.domain.dto.SignUpDTO;
import io.github.leehanryang.sundriesapi.domain.dto.TodoDTO;
import io.github.leehanryang.sundriesapi.surpport.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Todo API 통합 흐름 테스트
 * <p>
 * - 사용자 가입 및 로그인
 * - Todo 생성, 조회, 수정, 삭제, 검색
 * - 다른 사용자 접근 권한 검증
 */
class TodoFlowTest extends IntegrationTestSupport {

    private String jwt;
    private UUID todoId;
    private String username;
    private String email;

    @BeforeEach
    void setUp() throws Exception {
        // Given: 매번 유니크한 사용자 생성 및 로그인, 기본 Todo 생성
        String rand = UUID.randomUUID().toString().substring(0, 8);
        username = "testUser" + rand;
        email = "tester+" + rand + "@test.com";

        SignUpDTO signUp = SignUpDTO.builder()
                .username(username)
                .email(email)
                .password("password")
                .build();
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUp)))
                .andExpect(status().isCreated());

        LoginDTO login = new LoginDTO(null, null, email, "password", null);
        String body = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String rawToken = objectMapper.readTree(body).get("access_token").asText();
        jwt = "Bearer " + rawToken;

        TodoDTO defaultTodo = TodoDTO.builder()
                .title("기본 Todo")
                .description("기본값")
                .completed(false)
                .build();
        String location = mockMvc.perform(post("/todos")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultTodo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        assertThat(location).isNotBlank();
        todoId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    @DisplayName("Todo 생성")
    void createTodo() throws Exception {
        // Given: 새로운 Todo 요청 DTO
        TodoDTO req = TodoDTO.builder()
                .title("JUnit 공부")
                .description("MockMvc로 통합 테스트 작성")
                .completed(false)
                .build();

        // When: POST 요청
        String location = mockMvc.perform(post("/todos")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // Then: Location 헤더 유효 및 GET 요청으로 검증
        assertThat(location).isNotBlank();
        UUID createdId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
        mockMvc.perform(get("/todos/{id}", createdId)
                        .header("Authorization", jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("JUnit 공부"));
    }

    @Test
    @DisplayName("Todo 목록 조회")
    void listTodos() throws Exception {
        // When: GET /todos 호출
        String body = mockMvc.perform(get("/todos")
                        .header("Authorization", jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then: 리스트가 비어있지 않음
        List<?> list = objectMapper.readValue(body, List.class);
        assertThat(list).isNotEmpty();
    }

    @Test
    @DisplayName("Todo 수정")
    void updateTodo() throws Exception {
        // Given: 업데이트 요청 DTO
        TodoDTO updateReq = TodoDTO.builder()
                .id(todoId)
                .title("JUnit 통합 테스트")
                .description("MockMvc 흐름 테스트")
                .completed(true)
                .build();

        // When & Then: PUT /todos/{id} -> 응답 검증
        mockMvc.perform(put("/todos/{id}", todoId)
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("JUnit 통합 테스트"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @DisplayName("Todo 삭제")
    void deleteTodo() throws Exception {
        // When & Then: DELETE -> 204, 이어서 GET 시 404
        mockMvc.perform(delete("/todos/{id}", todoId)
                        .header("Authorization", jwt))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/todos/{id}", todoId)
                        .header("Authorization", jwt))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Todo 검색")
    void searchTodos() throws Exception {
        // When & Then: 검색 키워드로 GET /todos/search
        mockMvc.perform(get("/todos/search")
                        .param("keyword", "기본")
                        .header("Authorization", jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("기본 Todo"));
    }

    @Test
    @DisplayName("다른 사용자 Todo 조회/수정/삭제 시 접근 거부")
    void otherUserCannotAccessTodo() throws Exception {
        // Given: 다른 사용자 생성 및 로그인
        String rand2 = UUID.randomUUID().toString().substring(0, 8);
        SignUpDTO otherSignUp = SignUpDTO.builder()
                .username("otherUser" + rand2)
                .email("other+" + rand2 + "@test.com")
                .password("password")
                .build();
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherSignUp)))
                .andExpect(status().isCreated());

        LoginDTO otherLogin = new LoginDTO(null, null, otherSignUp.getEmail(), otherSignUp.getPassword(), null);
        String otherBody = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String otherToken = objectMapper.readTree(otherBody).get("access_token").asText();
        String otherJwt = "Bearer " + otherToken;

        // When: 다른 사용자로 GET/PUT/DELETE 시도
        // Then: 모두 403 Forbidden
        mockMvc.perform(get("/todos/{id}", todoId)
                        .header("Authorization", otherJwt))
                .andExpect(status().isForbidden());

        TodoDTO updateReq = TodoDTO.builder()
                .title("Fail")
                .description("Fail")
                .completed(false)
                .build();
        mockMvc.perform(put("/todos/{id}", todoId)
                        .header("Authorization", otherJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/todos/{id}", todoId)
                        .header("Authorization", otherJwt))
                .andExpect(status().isForbidden());
    }
}
