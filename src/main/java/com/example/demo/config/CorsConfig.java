package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * 요청 들어오는 순서 servlet container(바깥 문?) -> spring security 필터체인(입밴, 필터'체인'은 필터가 줄줄이 연결된 것)
     * -> dispacherServlet(spring mvc 진입점, 어떤 controller로 보낼지?) -> [container->service->db]

     * WebMvcConfigurer.addCorsMappings()는 DispatcherServlet 이후(MVC 레이어)에서만 동작하기 때문에,
     * Spring Security 필터 체인이 먼저 요청을 가로채는 경우 Preflight(OPTIONS) 요청이
     * CORS 헤더 없이 401/403으로 차단될 수 있음? -> 그래서 CorsConfigurationSource 빈을 만들어서 필터체인안에 cors처리 넣었다?
     * 그래서 option(preflight)요청이 시큐리티에서 차단되기 전에 아 이건 허락맡는 요청이구나 ok 통과
     *
     * SecurityConfig에서 .cors(cors -> cors.configurationSource(...))로 이 빈을 참조해야
     * Security 레이어와 MVC 레이어 양쪽에 동일한 CORS 정책이 적용된다.
     * Security는 DispatcherServlet보다 앞에 있어서, MVC 레벨 CORS 설정은 Security에 막히는 요청엔 적용이 안 된다는 게 중요해보임
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // React (CRA)
                "http://localhost:5173",   // React (Vite)
                "http://localhost:8080"    // 동일 서버 — Swagger UI 테스트용
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // allowedHeaders("*") 대신 필요한 헤더만 명시
        // Authorization: JWT Bearer 토큰 전달에 필수
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));

        // allowCredentials(true)는 allowedOrigins("*")와 함께 사용 불가
        // 반드시 구체적인 origin 목록과 함께 사용해야 한다
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 config 적용?
        return source;
    }
}