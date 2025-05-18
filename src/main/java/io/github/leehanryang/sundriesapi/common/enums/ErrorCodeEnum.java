package io.github.leehanryang.sundriesapi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCodeEnum {

    /* ───────── COMMON : 400 Bad Request ───────── */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력 값이 올바르지 않습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 형식이 잘못되었습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),

    /* ───────── AUTH : 4xx ───────── */
    /**
     * 접근 권한이 없습니다.
     */
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    /**
     * 이미 사용 중인 아이디
     */
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    /**
     * 이미 사용 중인 이메일
     */
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    /**
     * 비밀번호 규칙 위반
     */
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다."),
    /**
     * 로그인 실패 (이메일·비밀번호 불일치)
     */
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    /**
     * 인증 토큰이 없거나 잘못됨
     */
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    /**
     * 토큰 만료
     */
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인 세션이 만료되었습니다. 다시 로그인해 주세요."),
    /**
     * 권한 부족
     */
    ROLES_NOT_MATCH(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    /**
     * 존재하지 않는 회원
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
    /**
     * 지원되지 않는 OAuth2
     */
    UNSUPPORTED_PROVIDER(HttpStatus.NOT_FOUND, "지원되지 않는 OAuth2 입니다."),

    /* ───────── TODO : 4xx ───────── */
    /**
     * 중복 등록 방지
     */
    DUPLICATE_TODO(HttpStatus.CONFLICT, "이미 존재하는 항목입니다."),
    /**
     * GET /todos : 비어 있거나 삭제된 경우
     */
    TODO_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "리스트를 찾을 수 없습니다."),
    /**
     * GET /todos/{id}, /todos/search : 개별 상세
     */
    TODO_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다."),

    /* ───────── INTERNAL : 500 ───────── */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;
}