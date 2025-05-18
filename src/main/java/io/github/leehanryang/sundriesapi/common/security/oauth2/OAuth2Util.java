package io.github.leehanryang.sundriesapi.common.security.oauth2;

import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import io.github.leehanryang.sundriesapi.common.exception.ApiException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class OAuth2Util {

    public String generateRandomString() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public String extractUserName(String provider) {
        return provider + "_" + generateRandomString();
    }

    public String extractEmail(String provider, Map<String, Object> attr) {
        return switch (provider) {
            case "google" -> (String) attr.get("email");
            case "kakao" -> (String) ((Map<?, ?>) attr.get("kakao_account")).get("email");
            case "naver" -> (String) ((Map<?, ?>) attr.get("response")).get("email");
            default -> throw new ApiException(ErrorCodeEnum.UNSUPPORTED_PROVIDER);
        };
    }

    public String extractProviderId(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> (String) attributes.get("sub");
            case "kakao" -> String.valueOf(attributes.get("id"));
            case "naver" -> (String) ((Map<?, ?>) attributes.get("response")).get("id");
            default -> throw new ApiException(ErrorCodeEnum.UNSUPPORTED_PROVIDER);
        };
    }

    public String getNameAttributeKey(String provider) {
        return switch (provider) {
            case "google" -> "sub";
            case "kakao" -> "id";
            case "naver" -> "response";
            default -> throw new ApiException(ErrorCodeEnum.UNSUPPORTED_PROVIDER);
        };
    }
}
