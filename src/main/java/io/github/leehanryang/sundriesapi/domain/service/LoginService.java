package io.github.leehanryang.sundriesapi.domain.service;

import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import io.github.leehanryang.sundriesapi.common.exception.ApiException;
import io.github.leehanryang.sundriesapi.common.security.jwt.JwtUtil;
import io.github.leehanryang.sundriesapi.domain.dto.LoginDTO;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import io.github.leehanryang.sundriesapi.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtProvider;

    public String authenticate(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));
        String role = user.getRoles().iterator().next();

        dto.setUsername(user.getUsername());
        dto.setId(user.getId());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            // 아이디·비밀번호 검증 실패
            throw new ApiException(ErrorCodeEnum.LOGIN_FAILED);
        }

        return jwtProvider.generateToken(dto.getId(), dto.getUsername(), dto.getEmail(), role);
    }
}
