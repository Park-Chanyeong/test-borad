package com.example.demo.security;

import com.example.demo.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 HTTP 요청에서 JWT를 검사해 인증 상태를 설정하는 필터.
 *
 * OncePerRequestFilter를 상속하면 한 요청에 이 필터가 정확히 한 번만 실행
 * (리다이렉트 등으로 필터가 중복 실행되는 걸 방지)
 * SecurityConfig에서 UsernamePasswordAuthenticationFilter 앞에 등록되어 동작한다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * 요청마다 실행되는 필터 본체.
     *
     * 처리 흐름
     * 1. Authorization 헤더에서 토큰 추출
     * 2. 토큰이 있고 유효하면 username 꺼냄
     * 3. DB에서 해당 유저 정보 조회
     * 4. SecurityContext에 인증 정보 저장 → 이후 @AuthenticationPrincipal로 꺼낼 수 있음
     * 5. 다음 필터로 요청 전달 (chain.doFilter)
     *
     * 토큰이 없거나 유효하지 않으면 인증 저장 없이 그냥 다음 필터로 넘어간다.
     * → permitAll() 엔드포인트는 통과, authenticated() 엔드포인트는 Security가 401 반환
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsername(token);

            // DB에서 유저 정보 조회 (권한 목록 포함)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Spring Security 인증 객체 생성
            // 파라미터: (principal, credentials, authorities)
            // credentials(비밀번호)는 이미 토큰으로 인증했으므로 null
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 요청의 IP, 세션ID 등 부가 정보를 인증 객체에 추가
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext에 인증 정보 저장
            // 이 시점부터 이 요청은 "인증된 요청"으로 처리된다
            // 컨트롤러에서 @AuthenticationPrincipal로 꺼내는 값이 바로 여기서 저장한 userDetails
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 인증 여부와 관계없이 다음 필터로 반드시 넘겨야 한다
        chain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 문자열만 추출.
     *
     * HTTP 요청 헤더 형식: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * "Bearer " (7글자)를 제거하고 토큰 값만 반환한다.
     * 헤더가 없거나 Bearer로 시작하지 않으면 null 반환.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7); // "Bearer " 이후 문자열
        }
        return null;
    }
}