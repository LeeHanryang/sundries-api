package io.github.leehanryang.sundriesapi.security.oauth2;

import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import io.github.leehanryang.sundriesapi.common.exception.ApiException;
import io.github.leehanryang.sundriesapi.common.security.oauth2.OAuth2Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OAuth2Util 유틸리티 메서드 검증 테스트
 * <p>
 * - generateRandomString(): 길이 및 패턴 검증
 * - extractUserName(): provider 접두사 및 패턴 검증
 * - extractEmail(): 각 provider별 이메일 추출 검증
 * - extractProviderId(): 각 provider별 ID 추출 검증
 * - 예외 처리: 지원하지 않는 provider 시 ApiException 발생 검증
 */
class OAuth2UtilTest {

    private final OAuth2Util util = new OAuth2Util();

    @Test
    @DisplayName("generateRandomString(): 길이 8, 16진수 패턴")
    void generateRandomString_lengthAndPattern() {
        // When: 랜덤 문자열 생성
        String s = util.generateRandomString();

        // Then: null 아님, 길이는 8, 0-9a-f 패턴
        assertThat(s)
                .isNotNull()
                .hasSize(8)
                .matches("[0-9a-f]{8}");
    }

    @Test
    @DisplayName("extractUserName(): provider_prefix + '_' + 랜덤 8자리")
    void extractUserName_pattern() {
        // Given: provider 이름
        String provider = "google";

        // When: 사용자 이름 생성
        String name = util.extractUserName(provider);

        // Then: "google_[0-9a-f]{8}" 패턴
        assertThat(name)
                .matches("google_[0-9a-f]{8}");
    }

    @Test
    @DisplayName("extractEmail(): GOOGLE")
    void extractEmail_google() {
        // Given: GOOGLE provider 속성
        Map<String, Object> attrs = Map.of("email", "g@test.com");

        // When: 이메일 추출
        String email = util.extractEmail("google", attrs);

        // Then: "g@test.com"
        assertThat(email).isEqualTo("g@test.com");
    }

    @Test
    @DisplayName("extractEmail(): KAKAO")
    void extractEmail_kakao() {
        // Given: KAKAO provider 속성
        Map<String, Object> kakaoAccount = Map.of(
                "email", "k@test.com",
                "profile", Map.of("nickname", "카카오")
        );
        Map<String, Object> attrs = Map.of("kakao_account", kakaoAccount);

        // When: 이메일 추출
        String email = util.extractEmail("kakao", attrs);

        // Then: "k@test.com"
        assertThat(email).isEqualTo("k@test.com");
    }

    @Test
    @DisplayName("extractEmail(): NAVER")
    void extractEmail_naver() {
        // Given: NAVER provider 속성
        Map<String, Object> response = Map.of(
                "email", "n@test.com",
                "name", "네이버"
        );
        Map<String, Object> attrs = Map.of("response", response);

        // When: 이메일 추출
        String email = util.extractEmail("naver", attrs);

        // Then: "n@test.com"
        assertThat(email).isEqualTo("n@test.com");
    }

    @Test
    @DisplayName("extractProviderId(): GOOGLE")
    void extractProviderId_google() {
        // Given: GOOGLE provider 속성
        Map<String, Object> attrs = Map.of("sub", "g-id");

        // When: providerId 추출
        String id = util.extractProviderId("google", attrs);

        // Then: "g-id"
        assertThat(id).isEqualTo("g-id");
    }

    @Test
    @DisplayName("extractProviderId(): KAKAO")
    void extractProviderId_kakao() {
        // Given: KAKAO provider 속성
        Map<String, Object> attrs = Map.of("id", 12345);

        // When: providerId 추출
        String id = util.extractProviderId("kakao", attrs);

        // Then: "12345"
        assertThat(id).isEqualTo("12345");
    }

    @Test
    @DisplayName("extractProviderId(): NAVER")
    void extractProviderId_naver() {
        // Given: NAVER provider 속성
        Map<String, Object> resp = Map.of("id", "naver-id");
        Map<String, Object> attrs = Map.of("response", resp);

        // When: providerId 추출
        String id = util.extractProviderId("naver", attrs);

        // Then: "naver-id"
        assertThat(id).isEqualTo("naver-id");
    }

    @Test
    @DisplayName("extractEmail(): 지원하지 않는 provider → ApiException")
    void extractEmail_unsupported() {
        // When/Then: foo provider 시 ApiException
        assertThatThrownBy(() -> util.extractEmail("foo", Map.of()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus())
                            .isEqualTo(ErrorCodeEnum.UNSUPPORTED_PROVIDER.getStatus());
                    assertThat(apiEx.getErrorMessage())
                            .isEqualTo(ErrorCodeEnum.UNSUPPORTED_PROVIDER.getMessage());
                });
    }

    @Test
    @DisplayName("extractProviderId(): 지원하지 않는 provider → ApiException")
    void extractProviderId_unsupported() {
        // When/Then: foo provider 시 ApiException
        assertThatThrownBy(() -> util.extractProviderId("foo", Map.of()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus())
                            .isEqualTo(ErrorCodeEnum.UNSUPPORTED_PROVIDER.getStatus());
                    assertThat(apiEx.getErrorMessage())
                            .isEqualTo(ErrorCodeEnum.UNSUPPORTED_PROVIDER.getMessage());
                });
    }

    @Test
    @DisplayName("getNameAttributeKey(): 지원하지 않는 provider → ApiException")
    void getNameAttributeKey_unsupported() {
        // When/Then: foo provider 시 ApiException
        assertThatThrownBy(() -> util.getNameAttributeKey("foo"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus())
                            .isEqualTo(ErrorCodeEnum.UNSUPPORTED_PROVIDER.getStatus());
                    assertThat(apiEx.getErrorMessage())
                            .isEqualTo(ErrorCodeEnum.UNSUPPORTED_PROVIDER.getMessage());
                });
    }
}