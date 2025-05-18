package io.github.leehanryang.sundriesapi.api.entity;

import io.github.leehanryang.sundriesapi.common.enums.OAuth2Enum;
import io.github.leehanryang.sundriesapi.domain.entity.SocialAccount;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SocialAccountEntityTest {

    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_EMAIL = "tester@test.com";

    @PersistenceContext
    private EntityManager em;

    @DynamicPropertySource
    static void sqliteProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:file:testdb?mode=memory&cache=shared");
        registry.add("spring.datasource.driver-class-name",
                () -> "org.sqlite.JDBC");
    }

    @Test
    @DisplayName("of() 편의 메서드 + JPA 매핑 검증")
    void of_and_mapping() {
        // Given: User 엔티티 영속화
        User user = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);
        em.persist(user);

        // When: SocialAccount.of()로 엔티티 생성 및 영속화
        SocialAccount sa = SocialAccount.of(user, OAuth2Enum.NAVER, "naver-123");
        em.persist(sa);
        em.flush();
        em.clear();

        // Then: DB에서 조회 후 필드 값 검증
        SocialAccount loaded = em.find(SocialAccount.class, sa.getId());
        assertThat(loaded.getProvider()).isEqualTo(OAuth2Enum.NAVER);
        assertThat(loaded.getProviderId()).isEqualTo("naver-123");
        assertThat(loaded.getUser().getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("Builder 생성 검증")
    void builder() {
        // Given: User 엔티티 생성
        User user = User.create(TEST_USER, TEST_PASSWORD, TEST_EMAIL);

        // When: Builder로 SocialAccount 생성
        SocialAccount sa = SocialAccount.builder()
                .user(user)
                .provider(OAuth2Enum.GOOGLE)
                .providerId("google-abc")
                .build();

        // Then: Builder 결과 검증
        assertThat(sa.getUser()).isSameAs(user);
        assertThat(sa.getProvider()).isEqualTo(OAuth2Enum.GOOGLE);
        assertThat(sa.getProviderId()).isEqualTo("google-abc");
        assertThat(sa.getId()).isNull(); // 아직 영속화 전
    }
}
