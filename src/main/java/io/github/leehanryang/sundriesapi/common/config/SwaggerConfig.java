package io.github.leehanryang.sundriesapi.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                /* JWT Bearer 인증 스키마 */
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")

                ))
                /* 전역 Security 요구 사항 */
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("TodoList API")
                        .version("1.0")
                        .description("""
                                📝 할 일 관리 API 명세서
                                
                                ⚠️ JWT 인증이 필요한 API의 경우 Swagger UI에서는 토큰을 수동 입력해야 합니다.
                                - 'Authorize' 버튼을 눌러 Bearer 토큰을 입력하세요.
                                - 일부 파라미터(예: Jwt를 통한 인증 정보)는 문서에 명시되어 있지 않습니다.
                                """)
                        .contact(new Contact()
                                .name("이준혁")
                                .email("ballack02@naver.com")));
    }
}
