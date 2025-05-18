package io.github.leehanryang.sundriesapi.common.enums;

public enum OAuth2Enum {
    GOOGLE, KAKAO, NAVER, ANONYMOUS;

    public static OAuth2Enum from(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> GOOGLE;
            case "kakao" -> KAKAO;
            case "naver" -> NAVER;
            case "anonymous" -> ANONYMOUS;
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인: " + registrationId);
        };
    }
}
