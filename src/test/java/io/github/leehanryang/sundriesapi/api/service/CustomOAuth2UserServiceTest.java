package io.github.leehanryang.sundriesapi.api.service;

import io.github.leehanryang.sundriesapi.common.enums.OAuth2Enum;
import io.github.leehanryang.sundriesapi.common.enums.RoleEnum;
import io.github.leehanryang.sundriesapi.common.security.oauth2.OAuth2Util;
import io.github.leehanryang.sundriesapi.domain.dto.UserDTO;
import io.github.leehanryang.sundriesapi.domain.entity.SocialAccount;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import io.github.leehanryang.sundriesapi.domain.repository.SocialAccountRepository;
import io.github.leehanryang.sundriesapi.domain.repository.UserRepository;
import io.github.leehanryang.sundriesapi.domain.service.CustomOAuth2UserService;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * CustomOAuth2UserService 단위 테스트
 * <p>
 * - loadUser(): OAuth2User 반환 및 processOAuth2User 호출 검증
 * - processOAuth2User(): 기존 소셜 계정 존재, 이메일 사용자 존재, 신규 사용자 분기 검증
 */
class CustomOAuth2UserServiceTest {

    private final String username = "oauthUser";
    private final String email = "oauth@test.com";
    private final String encodedPassword = "encoded1234";
    private final String provider = "google";
    private final String providerId = "abc123";

    @InjectMocks
    CustomOAuth2UserService customOAuth2UserService;
    @Mock
    OAuth2Util oAuth2Util;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    OAuth2UserRequest userRequest;
    @Mock
    OAuth2User mockOAuth2User;
    @Mock
    ClientRegistration clientRegistration;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SocialAccountRepository socialAccountRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("loadUser()는 OAuth2User를 반환하고 userService를 호출한다")
    void loadUser_success() {
        // Given: OAuth2UserRequest 및 프로바이더 정보 설정
        String username = "google_user";

        User user = User.create(username, encodedPassword, email);
        when(userRepository.save(any())).thenReturn(user);
        Map<String, Object> attributes = Map.of("sub", providerId, "email", email);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(provider);
        when(oAuth2Util.extractProviderId(provider, attributes)).thenReturn(providerId);
        when(oAuth2Util.extractEmail(provider, attributes)).thenReturn(email);
        when(oAuth2Util.extractUserName(provider)).thenReturn(username);
        when(oAuth2Util.getNameAttributeKey(provider)).thenReturn("sub");
        when(passwordEncoder.encode(any())).thenReturn(encodedPassword);
        when(mockOAuth2User.getAttributes()).thenReturn(attributes);
        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(mockOAuth2User).when(spyService).delegateLoadUser(userRequest);

        // When: loadUser 호출
        OAuth2User result = spyService.loadUser(userRequest);

        // Then: processOAuth2User 호출 및 권한 검증
        verify(spyService).processOAuth2User(username, email, encodedPassword, provider, providerId);
        assertThat(result.getAttributes()).isEqualTo(attributes);
        assertThat(result.getAuthorities())
                .anyMatch(auth -> auth.getAuthority().equals(RoleEnum.USER.getRole()));
    }

    @Test
    @DisplayName("이미 등록된 소셜 계정이 있는 경우 기존 유저 반환")
    void existingSocialAccount() {
        // Given: 소셜 계정이 이미 존재하는 상황
        User user = User.create(username, encodedPassword, email);
        SocialAccount socialAccount = SocialAccount.of(user, OAuth2Enum.GOOGLE, providerId);
        when(socialAccountRepository.findByProviderAndProviderId(OAuth2Enum.GOOGLE, providerId))
                .thenReturn(Optional.of(socialAccount));

        // When: processOAuth2User 호출
        UserDTO result = customOAuth2UserService.processOAuth2User(
                username, email, encodedPassword, provider, providerId);

        // Then: 기존 유저 반환, 저장 메서드 호출 없음
        AssertionsForClassTypes.assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository, never()).save(any());
        verify(socialAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("기존 이메일 유저가 있고 소셜 계정만 연결하는 경우")
    void existingEmailUser() {
        // Given: 이메일 기반 사용자만 존재
        User user = User.create(username, encodedPassword, email);
        when(socialAccountRepository.findByProviderAndProviderId(OAuth2Enum.GOOGLE, providerId))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When: processOAuth2User 호출
        UserDTO result = customOAuth2UserService.processOAuth2User(
                username, email, encodedPassword, provider, providerId);

        // Then: 소셜 계정 저장만 호출, 사용자 저장은 미호출
        AssertionsForClassTypes.assertThat(result.getEmail()).isEqualTo(email);
        verify(socialAccountRepository).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("이메일도 소셜도 모두 없을 경우 새 유저 생성")
    void newUserAndSocialAccount() {
        // Given: 이메일 및 소셜 계정 모두 미존재
        when(socialAccountRepository.findByProviderAndProviderId(OAuth2Enum.GOOGLE, providerId))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        User savedUser = User.create(username, encodedPassword, email);
        when(userRepository.save(any())).thenReturn(savedUser);

        // When: processOAuth2User 호출
        UserDTO result = customOAuth2UserService.processOAuth2User(
                username, email, encodedPassword, provider, providerId);

        // Then: 사용자 및 소셜 계정 저장 모두 호출
        AssertionsForClassTypes.assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository).save(any());
        verify(socialAccountRepository).save(any());
    }
}
