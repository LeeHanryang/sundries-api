package io.github.leehanryang.sundriesapi.exception;

import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import io.github.leehanryang.sundriesapi.common.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApiException 생성자 커버리지 및 필드 검증 테스트
 * <p>
 * - ErrorCodeEnum 기반 생성자 검증
 * - HttpStatus, 메시지 생성자 검증
 * - HttpStatus, 메시지, 원인, 데이터 생성자 검증
 */
class ApiExceptionTest {

    @Test
    @DisplayName("ErrorCodeEnum 생성자: status, message, data 확인")
    void constructorWithErrorCodeEnum() {
        // Given: ErrorCodeEnum 설정
        ErrorCodeEnum code = ErrorCodeEnum.MISSING_TOKEN;

        // When: ApiException 생성
        ApiException ex = new ApiException(code);

        // Then: 상태 코드, 메시지, 추가 데이터 검증
        assertThat(ex.getStatus()).isEqualTo(code.getStatus());
        assertThat(ex.getErrorMessage()).isEqualTo(code.getMessage());
        assertThat(ex.getData()).isNull();
        assertThat(ex).hasMessage(code.getMessage());
    }

    @Test
    @DisplayName("HttpStatus, 메시지 생성자: status 및 message 확인")
    void constructorWithStatusAndMessage() {
        // Given: HttpStatus 및 커스텀 메시지
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String customMsg = "잘못된 요청입니다.";

        // When: ApiException 생성
        ApiException ex = new ApiException(status, customMsg);

        // Then: 상태 코드 및 메시지 검증
        assertThat(ex.getStatus()).isEqualTo(status);
        assertThat(ex.getErrorMessage()).isEqualTo(customMsg);
        assertThat(ex.getData()).isNull();
        assertThat(ex).hasMessage(customMsg);
    }

    @Test
    @DisplayName("HttpStatus, 메시지, 원인, data 생성자: 모든 필드 확인")
    void constructorWithAllParameters() {
        // Given: HttpStatus, 메시지, 예외 원인, 추가 데이터
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String msg = "서버 에러 발생";
        Throwable cause = new IllegalStateException("원인 예외");
        String payload = "추가 데이터";

        // When: ApiException 생성
        ApiException ex = new ApiException(status, msg, cause, payload);

        // Then: 모든 필드 검증
        assertThat(ex.getStatus()).isEqualTo(status);
        assertThat(ex.getErrorMessage()).isEqualTo(msg);
        assertThat(ex.getData()).isEqualTo(payload);
        assertThat(ex).hasMessage(msg);
        assertThat(ex.getCause()).isSameAs(cause);
    }
}