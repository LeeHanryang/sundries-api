package io.github.leehanryang.sundriesapi.domain.service;

import io.github.leehanryang.sundriesapi.common.enums.OAuth2Enum;
import io.github.leehanryang.sundriesapi.common.enums.RoleEnum;
import io.github.leehanryang.sundriesapi.common.security.oauth2.OAuth2Util;
import io.github.leehanryang.sundriesapi.domain.dto.UserDTO;
import io.github.leehanryang.sundriesapi.domain.entity.SocialAccount;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import io.github.leehanryang.sundriesapi.domain.repository.SocialAccountRepository;
import io.github.leehanryang.sundriesapi.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2Util oAuth2Util;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;


    protected OAuth2User delegateLoadUser(OAuth2UserRequest req) {
        return super.loadUser(req);
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oauth2User = delegateLoadUser(req);
        Map<String, Object> attr = oauth2User.getAttributes();

        String provider = req.getClientRegistration().getRegistrationId();
        String providerId = oAuth2Util.extractProviderId(provider, attr);
        String email = oAuth2Util.extractEmail(provider, attr);
        String username = oAuth2Util.extractUserName(provider);

        processOAuth2User(
                username,
                email,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                provider,
                providerId
        );

        return new DefaultOAuth2User(
                Set.of(RoleEnum.USER::getRole),
                attr,
                oAuth2Util.getNameAttributeKey(provider)
        );
    }

    @Transactional
    public UserDTO processOAuth2User(String username, String email, String encodedPassword,
                                     String provider, String providerId) {
        OAuth2Enum providerEnum = OAuth2Enum.from(provider);
        // 같은 소셜 계정이 등록되어 있는지 확인
        Optional<SocialAccount> existingSA =
                socialAccountRepository.findByProviderAndProviderId(providerEnum, providerId);
        if (existingSA.isPresent()) {
            return UserDTO.from(existingSA.get().getUser());
        }

        // 같은 이메일로 가입된 계정이 있는지 확인
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 신규 계정 생성
                    User newUser = User.create(username, encodedPassword, email);
                    newUser.addRole(RoleEnum.USER.getRole());
                    return userRepository.save(newUser);
                });

        // SocialAccount 등록
        socialAccountRepository.save(
                SocialAccount.of(user, providerEnum, providerId)
        );

        return UserDTO.from(user);
    }
}
