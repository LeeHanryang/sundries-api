package io.github.leehanryang.sundriesapi.common.security.principal;

import io.github.leehanryang.sundriesapi.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;


public record CustomUserPrincipal(UUID id,
                                  String username,
                                  String password,
                                  Set<String> roles) implements UserDetails {

    public static CustomUserPrincipal from(User user) {
        return new CustomUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRoles()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  //계정 만료 여부 (true면 만료되지 않음)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 계정 잠금 여부 (true면 잠겨 있지 않음)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 인증 정보 만료 여부 (true면 만료되지 않음)
    }

    @Override
    public boolean isEnabled() {
        return true;  // 계정 활성화 여부 (true면 활성화됨)
    }
}
