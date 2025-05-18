package io.github.leehanryang.sundriesapi.exception;

import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import io.github.leehanryang.sundriesapi.common.exception.ApiException;
import io.github.leehanryang.sundriesapi.common.exception.RestAccessDeniedHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * RestAccessDeniedHandler 동작 검증 테스트
 * <p>
 * - handle(): 인증은 되었으나 권한이 부족한 경우 ApiException(ROLES_NOT_MATCH) 발생
 */
class RestAccessDeniedHandlerTest {

    private final RestAccessDeniedHandler handler = new RestAccessDeniedHandler();

    @Test
    @DisplayName("handle() 호출 시 ApiException(ROLES_NOT_MATCH) 발생")
    void handleThrowsApiException() {
        // Given: 모의 HttpServletRequest, HttpServletResponse 및 AccessDeniedException
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AccessDeniedException cause = new AccessDeniedException("access denied");

        // When/Then: handle 실행 시 ApiException 발생 및 필드 검증
        assertThatThrownBy(() -> handler.handle(request, response, cause))
                // Then: ApiException 타입 및 상태, 메시지 검증
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(ErrorCodeEnum.ROLES_NOT_MATCH.getStatus());
                    assertThat(apiEx.getErrorMessage()).isEqualTo(ErrorCodeEnum.ROLES_NOT_MATCH.getMessage());
                    assertThat(apiEx.getData()).isNull();
                });
    }
}