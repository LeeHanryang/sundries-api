package io.github.leehanryang.sundriesapi.common.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }

    /**
     * @return Spring Security 에서 사용하는 권한 문자열
     */
    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return role;
    }
}