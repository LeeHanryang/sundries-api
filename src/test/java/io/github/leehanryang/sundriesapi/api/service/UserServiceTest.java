package io.github.leehanryang.sundriesapi.api.service;

import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import io.github.leehanryang.sundriesapi.common.enums.RoleEnum;
import io.github.leehanryang.sundriesapi.common.exception.ApiException;
import io.github.leehanryang.sundriesapi.domain.dto.SignUpDTO;
import io.github.leehanryang.sundriesapi.domain.dto.UserDTO;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import io.github.leehanryang.sundriesapi.domain.repository.UserRepository;
import io.github.leehanryang.sundriesapi.domain.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String TEST_EMAIL = "tester@test.com";
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("정상적인 회원가입")
    void register_success() {
        // Given: 유효한 회원가입 DTO
        SignUpDTO dto = SignUpDTO.builder()
                .username(TEST_USER)
                .password(TEST_PASSWORD)
                .email(TEST_EMAIL)
                .build();
        when(userRepository.existsByUsername(TEST_USER)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        // When: register 호출
        User user = User.create(TEST_USER, ENCODED_PASSWORD, TEST_EMAIL);
        user.addRole(RoleEnum.USER.getRole());
        when(userRepository.save(any())).thenReturn(user);
        UserDTO result = userService.register(dto);

        // Then: 결과 DTO 필드 검증
        assertThat(result.getUsername()).isEqualTo(TEST_USER);
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("회원가입 시 사용자명 중복 예외")
    void register_duplicateUsername() {
        // Given: 중복된 사용자명 존재
        SignUpDTO dto = SignUpDTO.builder()
                .username(TEST_USER)
                .password(TEST_PASSWORD)
                .email(TEST_EMAIL)
                .build();
        when(userRepository.existsByUsername(TEST_USER)).thenReturn(true);

        // When/Then: ApiException 발생
        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCodeEnum.DUPLICATE_USERNAME.getMessage());
    }

    @Test
    @DisplayName("회원가입 시 이메일 중복 예외")
    void register_duplicateEmail() {
        // Given: 이메일 중복
        SignUpDTO dto = SignUpDTO.builder()
                .username(TEST_USER)
                .password(TEST_PASSWORD)
                .email(TEST_EMAIL)
                .build();
        when(userRepository.existsByUsername(TEST_USER)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When/Then: ApiException 발생
        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCodeEnum.DUPLICATE_EMAIL.getMessage());
    }

    @Test
    @DisplayName("회원 조회 성공")
    void getUser_success() {
        // Given: 존재하는 사용자
        User user = User.create(TEST_USER, ENCODED_PASSWORD, TEST_EMAIL);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // When: getUser 호출
        UserDTO result = userService.getUser(USER_ID);

        // Then: DTO 필드 검증
        assertThat(result.getUsername()).isEqualTo(TEST_USER);
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("회원 조회 실패")
    void getUser_notFound() {
        // Given: 사용자 미존재
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When/Then: ApiException 발생
        assertThatThrownBy(() -> userService.getUser(USER_ID))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCodeEnum.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 수정 성공 - 사용자명, 이메일, 비밀번호 변경")
    void updateUser_success() {
        // Given: 기존 사용자 및 수정 DTO
        User user = User.create(TEST_USER, ENCODED_PASSWORD, TEST_EMAIL);
        UserDTO updateDto = UserDTO.builder()
                .username("newUser")
                .email("new@test.com")
                .password("newPassword")
                .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any())).thenReturn(user);

        // When: updateUser 호출
        UserDTO result = userService.updateUser(USER_ID, updateDto);

        // Then: DTO 필드 검증
        assertThat(result.getUsername()).isEqualTo("newUser");
        assertThat(result.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    @DisplayName("회원 수정 실패 - 사용자 없음")
    void updateUser_userNotFound() {
        // Given: 사용자 미존재
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        UserDTO dto = UserDTO.builder().username("updateUser").build();

        // When/Then: ApiException 발생
        assertThatThrownBy(() -> userService.updateUser(USER_ID, dto))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCodeEnum.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 수정 실패 - 사용자명 중복")
    void updateUser_duplicateUsername() {
        // Given: 사용자 및 중복된 username
        User user = User.create(TEST_USER, ENCODED_PASSWORD, TEST_EMAIL);
        UserDTO dto = UserDTO.builder().username("duplicateUser").build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("duplicateUser")).thenReturn(true);

        // When/Then: ApiException 발생
        assertThatThrownBy(() -> userService.updateUser(USER_ID, dto))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCodeEnum.DUPLICATE_USERNAME.getMessage());
    }

    @Test
    @DisplayName("회원 삭제 성공")
    void deleteUser_success() {
        // Given: 존재하는 사용자
        User user = User.create(TEST_USER, ENCODED_PASSWORD, TEST_EMAIL);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // When: deleteUser 호출
        userService.deleteUser(USER_ID);

        // Then: delete 메서드 호출 검증
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("회원 삭제 실패 - 사용자 없음")
    void deleteUser_notFound() {
        // Given: 사용자 미존재
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When/Then: ApiException 발생
        assertThatThrownBy(() -> userService.deleteUser(USER_ID))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCodeEnum.USER_NOT_FOUND.getMessage());
    }
}
