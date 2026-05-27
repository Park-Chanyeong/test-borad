package com.example.demo.service;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.dto.TokenDto;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    //회원가입 - 중복 체크 후 비밀번호 해시해서 저장

    public void register(RegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateException("이미 존재하는 사용자입니다: " + dto.getUsername());
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }

    /*
    login() 흐름:
      1. username으로 유저 조회 (없으면 404)
      2. 입력 비밀번호 vs DB 해시 비교 (passwordEncoder.matches)
      3. Access Token (JWT) + Refresh Token (UUID) 생성
      4. 기존 Refresh Token 삭제 → 새 것 저장 (중복 방지)
      5. 두 토큰을 TokenDto에 담아 반환
     */
    @Transactional
    public TokenDto login(LoginDto dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();

        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        // 여기서 dto에 담는구나
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
        refreshTokenRepository.save(refreshToken);

        return new TokenDto(accessToken, refreshTokenValue);
    }
    /*
    refresh() — Refresh Token으로 Access Token 재발급
      1. DB에서 Refresh Token 조회
      2. 만료 여부 확인 (expiryDate.isBefore(now))
      3. 만료됐으면 DB에서 삭제 후 예외
      4. 유효하면 새 Access Token 발급
     */
    public TokenDto refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadCredentialsException("유효하지 않은 Refresh Token입니다."));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException("Refresh Token이 만료되었습니다.");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(refreshToken.getUser().getUsername());
        return new TokenDto(newAccessToken, refreshTokenValue);
    }
}