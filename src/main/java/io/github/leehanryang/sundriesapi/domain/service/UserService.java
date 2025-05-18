package io.github.leehanryang.sundriesapi.domain.service;


import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import io.github.leehanryang.sundriesapi.common.enums.RoleEnum;
import io.github.leehanryang.sundriesapi.common.exception.ApiException;
import io.github.leehanryang.sundriesapi.domain.dto.SignUpDTO;
import io.github.leehanryang.sundriesapi.domain.dto.UserDTO;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import io.github.leehanryang.sundriesapi.domain.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO register(SignUpDTO dto) {
        // 사용자명 중복 체크
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ApiException(ErrorCodeEnum.DUPLICATE_USERNAME);
        }
        // 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ApiException(ErrorCodeEnum.DUPLICATE_EMAIL);
        }
        // 비밀번호 인코딩
        String encoded = passwordEncoder.encode(dto.getPassword());

        // User 엔티티 생성
        User user = User.create(dto.getUsername(), encoded, dto.getEmail());
        user.addRole(RoleEnum.USER.getRole());

        return userRepository.save(user).toDto();
    }

    public UserDTO getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)).toDto();
    }

    @Transactional
    public UserDTO updateUser(UUID id, @Valid UserDTO dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));

        /* 닉네임 변경 */
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new ApiException(ErrorCodeEnum.DUPLICATE_USERNAME);
            }
            user.changeUsername(dto.getUsername());
        }

        /* 이메일 변경 */
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new ApiException(ErrorCodeEnum.DUPLICATE_EMAIL);
            }
            user.changeEmail(dto.getEmail());
        }

        /* 비밀번호 변경 */
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String encoded = passwordEncoder.encode(dto.getPassword());
            user.changePassword(encoded);
        }

        return userRepository.save(user).toDto();
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
