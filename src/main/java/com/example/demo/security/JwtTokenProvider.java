package com.example.demo.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰의 생성 / 검증 / 파싱을 담당하는 컴포넌트.
 * Access Token은 서명된 JWT, Refresh Token은 단순 UUID로 구분해 관리.
 */
@Component
public class JwtTokenProvider {

    // application.properties의 jwt.secret 값을 주입
    // Base64로 인코딩된 비밀키 문자열
    @Value("${jwt.secret}")
    private String secret;

    // 토큰 만료 시간 (밀리초 단위, 예: 1800000 = 30분)
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Base64 인코딩된 secret 문자열을 디코딩해 HSHA 서명 키를 생성.
     * JWT 서명/검증에 사용되며 secret이 같아야 서명이 일치
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * 로그인 성공 시 발급하는 Access Token 생성.
     *
     * JWT 구조: header.payload.signature
     * - subject  : 토큰 주인을 식별하는 값 (여기선 username)
     * - issuedAt : 발급 시각
     * - expiration: 만료 시각 (현재 시각 + accessTokenExpiration)
     * - signWith : 서버만 아는 비밀키로 서명 → 위변조 방지
     */
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Refresh Token 생성.
     * Refresh Token은 JWT가 아니라 단순 UUID 문자열이다.
     * 서버 DB(refresh_token 테이블)에 저장해 Access Token 재발급 시 검증
     * 검증을 db조회로 해서 jwt형식으로 안해도 오케이?
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * JWT에서 username(subject) 추출.
     * 파싱 과정에서 서명 검증도 함께 수행되므로 위변조된 토큰은 예외가 발생한다.
     */
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();  // subject = generateAccessToken()에서 넣은 username
    }

    /**
     * 토큰 유효성 검증.
     * 서명 불일치, 만료, 형식 오류 등 모든 JWT 예외를 false로 처리한다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}