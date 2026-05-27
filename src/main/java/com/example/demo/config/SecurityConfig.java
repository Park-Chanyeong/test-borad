package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // CorsConfig에서 등록한 빈을 주입받아 Security 레이어에 CORS 정책을 연결한다
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * REST API / Swagger UI / H2 콘솔 요청을 처리하는 Security 필터 체인.
     *
     * - JWT Stateless 방식이므로 세션을 생성하지 않고 CSRF도 비활성화.
     * - Order(1): webFilterChain보다 먼저 평가되어 /api/**, /swagger-ui/**, /h2-console/** 를 담당.
     * - CORS는 Security 레이어에서도 적용되어야 Preflight(OPTIONS) 요청이 정상 처리된다.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**") // 이 체인이 담당한 url 범위
            .cors(cors -> cors.configurationSource(corsConfigurationSource)) // cors 설정 연결 bean넣었던거
            // REST API는 토큰 기반 인증이므로 CSRF 토큰 불필요
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin())) // 세션 정책 h2는 iframe이니까?(브라우저 안에 끼워진 페이지꼴)
            // 로그인 → 토큰 발급 → 서버는 아무것도 저장 안 함 → 매 요청마다 토큰 자체를 검증 방식이 stateless, (세션인증 안하고 매번jwt토큰 인증)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //  URL별 인증 규칙
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // H2 콘솔은 Spring MVC DispatcherServlet 외부의 서블릿이라
                // 일반 requestMatchers()의 MVC 패턴 매칭이 동작하지 않는다.
                // PathRequest.toH2Console()은 Spring Boot가 H2 콘솔 경로를 서블릿 기준으로 매칭해주는 공식 방법이다.
                .requestMatchers(PathRequest.toH2Console()).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); //JWT 필터 등록

        return http.build();
    }

    /**
     * Thymeleaf 웹 UI 요청을 처리하는 Security 필터 체인.
     *
     * - 세션/쿠키 기반 방식으로 동작.
     * - CSRF는 기본 활성화 상태 유지
     * - Order(2): apiFilterChain이 먼저 평가되고, /web/**, / 는 이 체인이 담당.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/web/**", "/") // 여긴 web ui쪽만
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/web/login", "/web/register", "/").permitAll() //로그인 회원가입 누구나 ok
                .requestMatchers(HttpMethod.GET, "/web/posts", "/web/posts/**").permitAll() //게시글 get까진 ok
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/web/login") // 리다이렉트시키기
                .loginProcessingUrl("/web/login")       // POST /web/login 을 Security가 직접 처리 (핸들러가 없어도 로그인이 되는 이유?-> 시큐리티가 통째로 처리)
                .defaultSuccessUrl("/web/posts", true)  // true: 로그인 전 방문 페이지 무시하고 항상 이동
                .failureUrl("/web/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/web/logout")
                .logoutSuccessUrl("/web/posts")
                .permitAll()
            );

        return http.build();
    }

    /**
     * 비밀번호 단방향 해시 인코더.
     *
     * BCrypt는 내부적으로 랜덤 salt를 생성하므로 동일한 원문도 매번 다른 해시값이 만들어진다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 이건 잘 모르겠네..
     * Spring Security 인증 처리의 진입점.
     *
     * AuthService에서 UsernamePasswordAuthenticationToken을 검증할 때 직접 호출한다.
     * AuthenticationConfiguration을 통해 빈을 가져오는 방식은 순환 참조를 방지하는 표준 패턴이다.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}