package io.github.leehanryang.sundriesapi.common.exception;

import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorMessage;
    private final Object data;   // 선택

    public ApiException(ErrorCodeEnum code) {
        super(code.getMessage());
        this.status = code.getStatus();
        this.errorMessage = code.getMessage();
        this.data = null;
    }

    public ApiException(HttpStatus status, String msg) {
        super(msg);
        this.status = status;
        this.errorMessage = msg;
        this.data = null;
    }

    public ApiException(HttpStatus status, String msg, Throwable cause, Object data) {
        super(msg, cause);
        this.status = status;
        this.errorMessage = msg;
        this.data = data;
    }
}
