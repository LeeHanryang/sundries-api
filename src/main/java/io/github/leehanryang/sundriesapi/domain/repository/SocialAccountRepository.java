package io.github.leehanryang.sundriesapi.domain.repository;

import io.github.leehanryang.sundriesapi.common.enums.OAuth2Enum;
import io.github.leehanryang.sundriesapi.domain.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, UUID> {
    Optional<SocialAccount> findByProviderAndProviderId(OAuth2Enum provider, String providerId);
}
