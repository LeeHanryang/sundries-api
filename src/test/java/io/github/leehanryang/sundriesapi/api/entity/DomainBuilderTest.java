package io.github.leehanryang.sundriesapi.api.entity;

import io.github.leehanryang.sundriesapi.common.enums.OAuth2Enum;
import io.github.leehanryang.sundriesapi.domain.entity.SocialAccount;
import io.github.leehanryang.sundriesapi.domain.entity.Todo;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 도메인 빌더 메서드 커버리지 테스트
 * <p>
 * - User.builder(), Todo.builder(), SocialAccount.builder()가 정상 동작하는지 검증
 */
class DomainBuilderTest {

    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_EMAIL = "tester@test.com";

    @Test
    @DisplayName("User.builder() 커버")
    void userBuilder() {
        // Given: 빌더를 통해 설정할 필드
        // When: User 객체 생성
        User user = User.builder()
                .username(TEST_USER)
                .password(TEST_PASSWORD)
                .email(TEST_EMAIL)
                .build();

        // Then: 각 필드 값이 올바르게 할당되고, ID는 null
        assertThat(user.getUsername()).isEqualTo(TEST_USER);
        assertThat(user.getPassword()).isEqualTo(TEST_PASSWORD);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getId()).isNull(); // id는 @UuidGenerator로 DB 삽입 시 생성
    }

    @Test
    @DisplayName("Todo.builder() 커버")
    void todoBuilder() {
        // Given: 작성자 User 엔티티
        User author = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);
        // When: Todo 객체 생성 via 빌더
        Todo todo = Todo.builder()
                .user(author)
                .title("T")
                .description("D")
                .completed(true)
                .build();

        // Then: author 일치, 필드 값 정확, ID는 null
        assertThat(todo.getUser()).isSameAs(author);
        assertThat(todo.getTitle()).isEqualTo("T");
        assertThat(todo.getDescription()).isEqualTo("D");
        assertThat(todo.isCompleted()).isTrue();
        assertThat(todo.getId()).isNull();
    }

    @Test
    @DisplayName("SocialAccount.builder() 커버")
    void socialAccountBuilder() {
        // Given: 작성자 User 엔티티
        User user = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);
        // When: SocialAccount 객체 생성 via 빌더
        SocialAccount sa = SocialAccount.builder()
                .user(user)
                .provider(OAuth2Enum.NAVER)
                .providerId("naver-123")
                .build();

        // Then: user, provider, providerId 올바른지, ID null 확인
        assertThat(sa.getUser()).isSameAs(user);
        assertThat(sa.getProvider()).isEqualTo(OAuth2Enum.NAVER);
        assertThat(sa.getProviderId()).isEqualTo("naver-123");
        assertThat(sa.getId()).isNull();
    }
}