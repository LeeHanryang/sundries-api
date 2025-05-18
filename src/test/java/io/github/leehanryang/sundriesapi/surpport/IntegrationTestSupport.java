package io.github.leehanryang.sundriesapi.surpport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

/**
 * 통합 테스트 지원용 베이스 클래스
 * <p>
 * - SpringBootTest로 애플리케이션 컨텍스트 로드
 * - MockMvc 자동 구성으로 HTTP 요청 테스트 지원
 * - SQLite 메모리 DB를 사용한 테스트 데이터베이스 설정
 * - 테스트 간 컨텍스트 초기화(@DirtiesContext)
 * - ObjectMapper를 활용한 JSON 변환 유틸 제공
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestSupport {

    /**
     * MockMvc: 컨트롤러 레이어 통합 테스트를 위한 HTTP 요청/응답 시뮬레이터
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * ObjectMapper: 테스트 중 DTO↔JSON 직렬화/역직렬화 지원
     */
    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * DynamicPropertySource를 이용해 테스트용 메모리 SQLite 데이터베이스 URL 설정
     *
     * @param registry Spring 환경 설정 레지스트리
     */
    @DynamicPropertySource
    static void sqliteMemoryDb(DynamicPropertyRegistry registry) {
        // UUID 기반 고유 DB 이름 생성: 여러 테스트 인스턴스 동시 실행 방지
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String dbName = "testdb_" + uuid;

        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:file:" + dbName + "?mode=memory&cache=shared");
        registry.add("spring.datasource.driver-class-name",
                () -> "org.sqlite.JDBC");
    }

    /**
     * 객체를 JSON 문자열로 변환하는 헬퍼 메서드
     *
     * @param obj 변환 대상 객체
     * @return JSON 문자열
     * @throws Exception 변환 실패 시 예외
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}