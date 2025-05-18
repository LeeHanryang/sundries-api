package io.github.leehanryang.sundriesapi.domain.dto;

import io.github.leehanryang.sundriesapi.domain.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Getter
@Builder
public class UserDTO {

    private final UUID id;
    @NotBlank(message = "사용자 이름을 입력해주세요.")
    private final String username;
    private final String password;
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private final String email;
    private final Set<String> roles;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /* ───────── Entity → DTO 매핑 ───────── */
    public static UserDTO from(User entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .roles(entity.getRoles())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
