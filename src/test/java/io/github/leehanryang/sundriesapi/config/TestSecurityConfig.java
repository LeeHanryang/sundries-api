package io.github.leehanryang.sundriesapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 테스트 환경에서 Security 설정을 우회하기 위한 구성 클래스입니다.
 * <p>
 * - 모든 인증 예외 시 AuthenticationEntryPoint 대신 TestAuthenticationEntryPoint를 사용하여
 * 예외를 던지지 않고 401 상태 코드와 커스텀 메시지를 반환합니다.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * 테스트용 AuthenticationEntryPoint 빈 등록.
     *
     * @return TestAuthenticationEntryPoint 인스턴스
     */
    @Bean
    @Primary   // 동일 타입의 빈이 둘 이상일 때 우선 적용
    public AuthenticationEntryPoint testAuthenticationEntryPoint() {
        return new TestAuthenticationEntryPoint();
    }

    /**
     * 테스트용 EntryPoint – 인증 실패 시 예외를 던지지 않고
     * 단순히 401 Unauthorized 응답을 전송합니다.
     */
    static class TestAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request,
                             HttpServletResponse response,
                             AuthenticationException authException) throws IOException {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "유효하지 않은 토큰(테스트)");
        }
    }
}
