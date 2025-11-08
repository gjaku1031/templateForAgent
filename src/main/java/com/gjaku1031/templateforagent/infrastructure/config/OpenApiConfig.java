package com.gjaku1031.templateforagent.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger 설정
 * <p>JWT Bearer 인증 스키마를 등록해 Swagger UI에서 Authorization 헤더를 사용할 수 있게 함.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("templateForAgent API")
                        .version("v1")
                        .description("Spring Security(JWT) 기반 API. Swagger의 Authorize 버튼으로 Bearer 토큰을 설정하세요."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                );
    }
}

