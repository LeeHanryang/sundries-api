package io.github.leehanryang.sundriesapi.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 토큰 존재 여부에 따라 오류 코드 결정
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        ErrorCodeEnum errorCode = (header == null || !header.startsWith("Bearer "))
                ? ErrorCodeEnum.MISSING_TOKEN
                : ErrorCodeEnum.INVALID_TOKEN;

        // HTTP 상태‧헤더 지정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // 401
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 응답 바디 구성
        Map<String, Object> body = new HashMap<>();
        body.put("code", errorCode.name());
        body.put("message", errorCode.getMessage());

        // 직렬화 및 전송
        OBJECT_MAPPER.writeValue(response.getOutputStream(), body);
        response.flushBuffer();   // 반드시 flush
    }
}