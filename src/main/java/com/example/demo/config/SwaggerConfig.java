package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.stereotype.Component;

// @Bean 메서드가 없으므로 @Configuration이 아닌 @Component가 의미상 정확하다.
// springdoc은 @OpenAPIDefinition, @SecurityScheme 애노테이션을 스프링 빈 스캔 시점에 처리하므로
// 컴포넌트로 등록되기만 하면 동작한다.
@Component
@OpenAPIDefinition(
        info = @Info(
                title = "게시판 API 목록",
                version = "v1",
                description = "Spring Boot 게시판 REST API"
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "로그인 후 발급된 Access Token을 입력하세요. (Bearer 접두사 불필요)"
) // 토큰 집어 넣으면 이후 요청에 authorization 헤더에 자동 추가
public class SwaggerConfig {
}
