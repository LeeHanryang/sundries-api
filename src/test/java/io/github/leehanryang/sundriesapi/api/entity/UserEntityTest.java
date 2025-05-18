package io.github.leehanryang.sundriesapi.api.entity;

import io.github.leehanryang.sundriesapi.common.enums.OAuth2Enum;
import io.github.leehanryang.sundriesapi.domain.dto.UserDTO;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * User Entity 관련 기능 통합 테스트
 * <p>
 * - create(): 기본 필드 초기화 검증
 * - addSocialAccount/removeSocialAccount: 소셜 계정 관리 검증
 * - addRole/removeRole: 권한 추가·제거 검증
 * - 필드 변경(changeUsername, changePassword, changeEmail) 검증
 * - toDto(): DTO 변환 기능 검증
 */
class UserEntityTest {

    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_EMAIL = "tester@test.com";

    @Test
    @DisplayName("User.create() 기본 필드 검증")
    void create_defaults() {
        // When: 기본 생성 메서드 호출
        User user = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);

        // Then: 필드 초기값 확인, 소셜·역할 컬렉션 비어있음
        assertThat(user.getUsername()).isEqualTo(TEST_USER);
        assertThat(user.getPassword()).isEqualTo(TEST_PASSWORD);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getSocialAccounts()).isEmpty();
        assertThat(user.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("소셜 계정·권한 추가/제거 편의 메서드")
    void socialAndRole_management() {
        // Given: 초기 User 엔티티
        User user = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);

        // When: 소셜 계정 추가
        user.addSocialAccount(OAuth2Enum.NAVER, "naver-999");

        // Then: 소셜 계정 정보 검증
        assertThat(user.getSocialAccounts())
                .extracting("provider", "providerId")
                .containsExactly(tuple(OAuth2Enum.NAVER, "naver-999"));

        // When: 역할 추가
        user.addRole("ROLE_USER");
        // Then: 역할 목록에 포함
        assertThat(user.getRoles()).contains("ROLE_USER");

        // When: 역할 제거
        user.removeRole("ROLE_USER");
        // Then: 역할 목록에서 제외
        assertThat(user.getRoles()).doesNotContain("ROLE_USER");
    }

    @Test
    @DisplayName("필드 변경 메서드 검증")
    void field_change_methods() {
        // Given: 초기 User 엔티티
        User user = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);

        // When: 필드 변경 메서드 호출
        user.changeUsername("newName");
        user.changePassword("newPass");
        user.changeEmail("new@test.com");

        // Then: 변경된 필드 값 검증
        assertThat(user.getUsername()).isEqualTo("newName");
        assertThat(user.getPassword()).isEqualTo("newPass");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    @DisplayName("toDto() 변환 검증")
    void toDto_conversion() {
        // Given: User 엔티티에 역할 추가
        User user = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);
        user.addRole("ROLE_ADMIN");

        // When: DTO 변환
        UserDTO dto = user.toDto();

        // Then: DTO 필드 매핑 검증
        assertThat(dto.getUsername()).isEqualTo(TEST_USER);
        assertThat(dto.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(dto.getRoles()).contains("ROLE_ADMIN");
    }
}