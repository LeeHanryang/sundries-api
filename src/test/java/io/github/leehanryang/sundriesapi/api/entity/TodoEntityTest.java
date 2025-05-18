package io.github.leehanryang.sundriesapi.api.entity;

import io.github.leehanryang.sundriesapi.domain.dto.TodoDTO;
import io.github.leehanryang.sundriesapi.domain.entity.Todo;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Todo Entity 관련 기능 통합 테스트
 * <p>
 * - create(): 엔티티 생성 기능
 * - update(): 필드 수정 기능
 * - toDto(): DTO 변환 기능
 */
class TodoEntityTest {

    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_EMAIL = "tester@test.com";

    @Test
    @DisplayName("Todo.create() / update() 동작 검증")
    void create_and_update() {
        // Given: User 엔티티 생성
        User author = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);

        // When: 초기 Todo 생성
        Todo todo = Todo.create(author, "제목", "내용", false);

        // Then: 생성된 엔티티 상태 검증
        assertThat(todo.getUser()).isSameAs(author);
        assertThat(todo.getTitle()).isEqualTo("제목");
        assertThat(todo.getDescription()).isEqualTo("내용");
        assertThat(todo.isCompleted()).isFalse();

        // When: 엔티티 업데이트
        todo.update("제목2", "내용2", true);

        // Then: 업데이트 후 상태 검증
        assertThat(todo.getTitle()).isEqualTo("제목2");
        assertThat(todo.getDescription()).isEqualTo("내용2");
        assertThat(todo.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("toDto() 변환 검증")
    void toDto_conversion() {
        // Given: User 및 Todo 엔티티 생성
        User author = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);
        Todo todo = Todo.create(author, "T", "C", true);

        // When: 엔티티를 DTO로 변환
        TodoDTO dto = todo.toDto();

        // Then: 변환된 DTO 필드 검증
        assertThat(dto.getId()).isEqualTo(todo.getId());
        assertThat(dto.getTitle()).isEqualTo("T");
        assertThat(dto.getDescription()).isEqualTo("C");
        assertThat(dto.isCompleted()).isTrue();
    }
}
