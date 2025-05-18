```gradle
plugins {
id 'org.springframework.boot' version '3.4.5'
id 'io.spring.dependency-management' version '1.1.4'
id 'java'

id 'com.ewerk.gradle.plugins.querydsl' version '1.0.10'
}

java {
sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
mavenCentral()
}

dependencies {
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-validation'

    runtimeOnly   'org.xerial:sqlite-jdbc:3.45.2.0'

    implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
    runtimeOnly   'io.jsonwebtoken:jjwt-impl:0.12.5'
    runtimeOnly   'io.jsonwebtoken:jjwt-jackson:0.12.5'

    implementation     'com.querydsl:querydsl-jpa:5.1.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'

}

tasks.named('test') {
useJUnitPlatform()
}

def querydslDir = "$buildDir/generated/querydsl"
querydsl {
jpa = true
querydslSourcesDir = querydslDir
}
sourceSets.main.java.srcDirs += [ querydslDir ]
```

---
**File:** `TodoController.java`
---

```java

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoRequest request) {
        TodoResponse created = todoService.create(request);
        return ResponseEntity
                .created(URI.create("/todos/" + created.id()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> findAll() {
        return ResponseEntity.ok(todoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TodoRequest request) {
        return ResponseEntity.ok(todoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<TodoResponse>> search(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(todoService.search(keyword));
    }
}
```

---
**File:** `UserController.java`
---

```java

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signUp(@Valid @RequestBody SignUpRequest req) {
        UserDTO created = userService.register(req.username(), req.email(), req.password());
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = authService.authenticate(req.username(), req.password());
        return ResponseEntity.ok(new LoginResponse(token, CommonMessage.SUCCESS.getMessage()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.id()));
    }


    public record SignUpRequest(
            @NotBlank String username,
            @Email String email,
            @NotBlank String password
    ) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String accessToken,
            String message
    ) {
    }
}
```

---
**File:** `Todo.java`
---

```java
public class Todo {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean completed;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /* 상태 변경 편의 메서드 */
    public void update(String title, String content, boolean completed) {
        this.title = title;
        this.content = content;
        this.completed = completed;
    }
}
```

---
**File:** `User.java`
---

```java
public class User {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 30)
    private final Set<String> roles = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 비밀번호 교체 */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /** 이메일 변경 */
    public void changeEmail(String email) {
        this.email = email;
    }

    /** 권한 추가 */
    public void addRole(String role) {
        this.roles.add(role);
    }

    /** 권한 제거 */
    public void removeRole(String role) {
        this.roles.remove(role);
    }
}
```

---
**File:** `TodoDTO.java`
---

```java
public class TodoDTO {

    private final UUID id;
    private final String userId;
    private final String title;
    private final String content;
    private final boolean completed;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static TodoDTO from(Todo entity) {
        return TodoDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .completed(entity.isCompleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public Todo toEntity() {
        return Todo.builder()
                .id(this.id)
                .userId(this.userId)
                .title(this.title)
                .content(this.content)
                .completed(this.completed)
                .build();
    }
}
```

---
**File:** `UserDTO.java`
---

```java
public class UserDTO {
    private final UUID id;
    private final String username;
    private final String email;
    private final Set<String> roles;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static UserDTO from(User entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .roles(entity.getRoles())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public User toEntity(String encodedPw) {
        return User.builder()
                .id(this.id)
                .username(this.username)
                .password(encodedPw)
                .email(this.email)
                .roles(this.roles)
                .build();
    }
}
```

---
**File:** `SocialAccount.java`
---

```java
public class SocialAccount {
    private UUID id;
    private String provider;
    private String providerId;
    private User user;
}
```

---
**File:** `ApiResponse.java`
---

```java
public class ApiResponse<T> {
    private final boolean success;
    private final int status;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .data(data)
                .build();
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static ApiResponse<Void> of(HttpStatus status, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .status(status.value())
                .message(message)
                .build();
    }
}
```

---
**File:** `GlobalExceptionHandler.java`
---

```java
public class GlobalExceptionHandler {
    public ApiResponse<Void> handleBaseException(BaseException ex) {
        log.warn("Handled BaseException: {}", ex.getErrorMessage());
        return ApiResponse.of(ex.getStatus(), ex.getErrorMessage());
    }

    public ApiResponse<Void> handleValidation(Exception ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ApiResponse.of(HttpStatus.BAD_REQUEST, "입력 값이 올바르지 않습니다.");
    }

    public ApiResponse<Void> handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        return ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
    }
}
```

---
**File:** `CustomOAuth2UserService.java`
---

```java
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oauth2User = super.loadUser(req);
        String provider = req.getClientRegistration().getRegistrationId();
        Map<String, Object> attr = oauth2User.getAttributes();
        String providerId = switch (provider) {
            case "google" -> (String) attr.get("sub");
            case "github" -> String.valueOf(attr.get("id"));
            case "kakao" -> String.valueOf(((Map<?, ?>) attr.get("kakao_account")).get("id"));
            default -> throw new IllegalArgumentException("지원하지 않는 소셜: " + provider);
        };

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> registerNewSocialUser(provider, providerId, attr));
        user.updateSocialInfo(provider, providerId);
        userRepository.save(user);

        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new SocialUserPrincipal(user, attr, authorities);
    }

    private User registerNewSocialUser(String provider,
                                       String providerId,
                                       Map<String, Object> attr) {
        String email = extractEmail(provider, attr);
        String username = provider + "_" + email;
        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .roles(Set.of("ROLE_USER"))
                .build();
        return userRepository.save(newUser);
    }

    private String extractEmail(String provider, Map<String, Object> attr) {
        return switch (provider) {
            case "google", "github" -> (String) attr.get("email");
            case "kakao" -> (String) ((Map<?, ?>) attr.get("kakao_account")).get("email");
            default -> throw new IllegalArgumentException("이메일 추출 실패: " + provider);
        };
    }
}
```

---
**File:** `SocialUserPrincipal.java`
---

```java
public class SocialUserPrincipal implements OAuth2User, UserDetails {
    private final User user;
    private final Map<String, Object> attributes;
    private final Set<GrantedAuthority> authorities;

    public SocialUserPrincipal(User user,
                               Map<String, Object> attributes,
                               Set<GrantedAuthority> authorities) {
        this.user = user;
        this.attributes = attributes;
        this.authorities = authorities;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getName() {
        return user.getProviderId();
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getPassword() {
        return user.getPassword();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public UUID getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }
}
```

---
**File:** `OAuth2AuthenticationSuccessHandler.java`
---

```java

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;
    private final String redirectUri = "http://localhost:5173/oauth2/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String username = authentication.getName();
        String token = authService.generateToken(username);
        URI target = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build()
                .toUri();
        response.sendRedirect(target.toString());
    }
}
```

---
**File:** `RestAuthenticationEntryPoint.java`
---

```java

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) {
        throw new BaseException(ErrorCodeEnum.INVALID_TOKEN.getStatus(),
                ErrorCodeEnum.INVALID_TOKEN.getMessage());
    }
}
```

---
**File:** `RestAccessDeniedHandler.java`
---

```java

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        throw new BaseException(ErrorCodeEnum.ACCESS_DENIED.getStatus(),
                ErrorCodeEnum.ACCESS_DENIED.getMessage());
    }
}
```

---
**File:** `SecurityConfig.java`
---

```java

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    // ... (PasswordEncoder, UserDetailsService, JwtFilter 등)

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/users/login", "/users/signup").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS));

        return http.build();
    }
}
```

---
**File:** `OAuth2AuthenticationFailureHandler.java`
---

```java
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/login?error=true")
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}

```

> **⚠️ 주의 사항**
>
> - LLM을 통한 line 단위 수정 내용은 첨부하지 못하였습니다.
> - 테스트 코드 및 가독성 강화를 위한 주석 작성에 도움을 받았습니다.
> - 감사합니다.