package io.github.leehanryang.sundriesapi.domain.entity;

import io.github.leehanryang.sundriesapi.domain.dto.TodoDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "todos_tbl")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Todo {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean completed;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    /* 상태 변경 편의 메서드 */
    public static Todo create(User author,
                              String title,
                              String content,
                              boolean completed) {

        Todo todo = new Todo();
        todo.user = author;      // 연관관계 주입
        todo.title = title;
        todo.description = content;
        todo.completed = completed;
        return todo;
    }

    public void update(String title, String content, boolean completed) {
        this.title = title;
        this.description = content;
        this.completed = completed;
    }

    /* DTO 변환 메서드 */
    public TodoDTO toDto() {
        return TodoDTO.from(this);
    }

}
