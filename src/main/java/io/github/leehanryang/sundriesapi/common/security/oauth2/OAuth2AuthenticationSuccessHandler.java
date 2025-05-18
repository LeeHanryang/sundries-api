package io.github.leehanryang.sundriesapi.common.security.oauth2;

import io.github.leehanryang.sundriesapi.common.security.jwt.JwtUtil;
import io.github.leehanryang.sundriesapi.domain.dto.UserDTO;
import io.github.leehanryang.sundriesapi.domain.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2Util oAuth2Util;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // OAuth2 provider 정보 가져오기
        String provider = request.getRequestURI().split("/")[4]; // /login/oauth2/code/{provider}
        Map<String, Object> attr = oauth2User.getAttributes();

        String providerId = oAuth2Util.extractProviderId(provider, attr);
        String email = oAuth2Util.extractEmail(provider, attr);
        String username = oAuth2Util.extractUserName(provider);

        // OAuth2 제공자로부터 받은 정보로 사용자 조회 또는 생성
        UserDTO user = customOAuth2UserService.processOAuth2User(
                username,
                email,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                provider,
                providerId
        );

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().iterator().next()
        );

        // 프론트엔드 리다이렉트 URL에 토큰 추가
        String targetUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl)
                .path("/login/oauth2/code/{provider}")
                .queryParam("token", token)
                .buildAndExpand(provider)
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}