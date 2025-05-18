package io.github.leehanryang.sundriesapi.domain.dto;

import io.github.leehanryang.sundriesapi.domain.entity.Todo;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Builder
public class TodoDTO {

    private final UUID id;
    @NotBlank
    @Size(max = 100)
    private final String title;
    private final String description;
    private final boolean completed;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /* ───────── Entity → DTO 매핑 ───────── */
    public static TodoDTO from(Todo entity) {
        return TodoDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .completed(entity.isCompleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /* 필요 시 DTO → Entity 변환용 */
    public Todo toEntity(User user) {
        return Todo.create(user, title, description, completed);

    }
}
