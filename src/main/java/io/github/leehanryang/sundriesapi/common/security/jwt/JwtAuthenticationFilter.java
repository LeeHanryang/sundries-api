package io.github.leehanryang.sundriesapi.common.security.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final List<String> EXCLUDE_URLS = List.of(
            "/oauth2/authorize",   // OAuth2 인가 요청
            "/oauth2/redirect",    // OAuth2 콜백
            "/login/oauth2",        // OAuth2 콜백 (대체 URI)
            "/login/oauth2/code"  // 추가 필요

    );

    private final JwtUtil jwtUtil;    // 주입 필요

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return EXCLUDE_URLS.stream()
                .anyMatch(path::startsWith) || path.startsWith("/login/oauth2/code/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);  // "Bearer " 제거
            if (jwtUtil.validate(token)) {
                SecurityContextHolder.getContext()
                        .setAuthentication(jwtUtil.toAuthentication(token));
            }
        }
        chain.doFilter(request, response);
    }
}