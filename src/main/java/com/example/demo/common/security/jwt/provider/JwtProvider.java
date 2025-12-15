package com.example.demo.common.security.jwt.provider;

import static com.example.demo.common.security.constant.SecurityConst.JWT_AUTHORITIES_KEY;
import static com.example.demo.common.security.constant.SecurityConst.JWT_USERNAME_KEY;
import static com.example.demo.infra.redis.constant.RedisConst.REDIS_REFRESH_TOKEN_PREFIX;

import com.example.demo.common.security.model.CustomUserDetails;
import com.example.demo.domain.account.model.AccountRole;
import com.example.demo.infra.redis.dao.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.example.demo.common.security.jwt.provider
 * FileName    : JwtProvider
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : JWT 생성 및 검증
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Component
@Slf4j
public class JwtProvider {

    private final String    issuer;
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final long      accessTokenExpirationMillis;
    private final long      refreshTokenExpirationMillis;

    private final RedisRepository redisRepository;

    public JwtProvider(@Value("${jwt.issuer}") final String issuer,
                       @Value("${jwt.access-token.secret}") final String accessTokenSecret,
                       @Value("${jwt.access-token.expiration-seconds}") final long accessTokenExpirationSeconds,
                       @Value("${jwt.refresh-token.secret}") final String refreshTokenSecret,
                       @Value("${jwt.refresh-token.expiration-seconds}") final long refreshTokenExpirationSeconds,
                       final RedisRepository redisRepository) {
        this.issuer = issuer;
        accessTokenKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessTokenSecret));
        accessTokenExpirationMillis = accessTokenExpirationSeconds * 1000L;
        refreshTokenKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshTokenSecret));
        refreshTokenExpirationMillis = refreshTokenExpirationSeconds * 1000L;
        this.redisRepository = redisRepository;
    }

    /**
     * AccessToken을 생성합니다.
     *
     * @param authentication - Authentication 객체
     * @return JWT AccessToken
     */
    public String generateAccessToken(final Authentication authentication) {
        CustomUserDetails userDetails = getCustomUserDetails(authentication);

        String id    = userDetails.getId().toString();
        String email = userDetails.getUsername();
        String role  = userDetails.getRole().name();

        Date now                  = new Date(System.currentTimeMillis());
        Date accessTokenExpiresIn = new Date(now.getTime() + accessTokenExpirationMillis);

        return getJwtBuilder().subject(id)
                              .claim(JWT_USERNAME_KEY, email)
                              .claim(JWT_AUTHORITIES_KEY, role)
                              .issuer(issuer)
                              .issuedAt(now)
                              .expiration(accessTokenExpiresIn)
                              .signWith(accessTokenKey, SIG.HS512)
                              .compact();
    }

    /**
     * RefreshToken을 생성합니다.
     *
     * @param authentication - Authentication 객체
     * @return JWT RefreshToken
     */
    public String generateRefreshToken(final Authentication authentication) {
        CustomUserDetails userDetails = getCustomUserDetails(authentication);

        String id    = userDetails.getId().toString();
        String email = userDetails.getUsername();
        String role  = userDetails.getRole().name();

        Date now                   = new Date(System.currentTimeMillis());
        Date refreshTokenExpiresIn = new Date(now.getTime() + refreshTokenExpirationMillis);

        String refreshToken = getJwtBuilder().subject(id)
                                             .claim(JWT_USERNAME_KEY, email)
                                             .claim(JWT_AUTHORITIES_KEY, role)
                                             .issuer(issuer)
                                             .issuedAt(now)
                                             .expiration(refreshTokenExpiresIn)
                                             .signWith(refreshTokenKey, SIG.HS512)
                                             .compact();

        String redisKey = REDIS_REFRESH_TOKEN_PREFIX + id;
        redisRepository.setValue(redisKey, refreshToken, Duration.ofMillis(refreshTokenExpirationMillis));

        return refreshToken;
    }

    /**
     * AccessToken으로부터 Authentication 객체를 생성합니다.
     *
     * @param accessToken - JWT AccessToken
     * @return Authentication 객체
     */
    public Authentication getAuthenticationFromAccessToken(final String accessToken) {
        Claims claims = getClaims(accessToken, accessTokenKey);

        UUID        id    = UUID.fromString(claims.getSubject());
        String      email = claims.get(JWT_USERNAME_KEY, String.class);
        AccountRole role  = AccountRole.valueOf(claims.get(JWT_AUTHORITIES_KEY, String.class));

        UserDetails userDetails = CustomUserDetails.of(id, email, null, role, null);
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role.getRoleValue())
        );

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    /**
     * RefreshToken으로부터 Authentication 객체를 생성합니다.
     *
     * @param refreshToken - JWT RefreshToken
     * @return Authentication 객체
     */
    public Authentication getAuthenticationFromRefreshToken(final String refreshToken) {
        Claims claims = getClaims(refreshToken, refreshTokenKey);

        UUID        id    = UUID.fromString(claims.getSubject());
        String      email = claims.get(JWT_USERNAME_KEY, String.class);
        AccountRole role  = AccountRole.valueOf(claims.get(JWT_AUTHORITIES_KEY, String.class));

        UserDetails userDetails = CustomUserDetails.of(id, email, null, role, null);
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role.getRoleValue())
        );

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    /**
     * AccessToken 유효성을 검증합니다.
     *
     * @param token - 검증할 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateAccessToken(final String token) {
        return validateToken(token, accessTokenKey);
    }

    /**
     * RefreshToken 유효성을 검증합니다.
     *
     * @param token - 검증할 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateRefreshToken(final String token) {
        return validateToken(token, refreshTokenKey);
    }

    /**
     * RefreshToken 유효 시간을 초(s) 단위로 반환합니다.
     *
     * @return RefreshToken 유효 시간(s)
     */
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationMillis / 1000L;
    }

    /**
     * RefreshToken으로부터 회원 ID를 추출합니다.
     *
     * @param token - 토큰
     * @return 유효하면 회원 ID, 아니면 null
     */
    public UUID getMemberIdFromRefreshToken(final String token) {
        if (!validateToken(token, refreshTokenKey)) return null;
        return UUID.fromString(getClaims(token, refreshTokenKey).getSubject());
    }

    /**
     * 토큰의 남은 만료 시간을 초 단위로 반환합니다.
     *
     * @param token - 토큰
     * @return 남은 유효 시간 (s)
     */
    public long getRemainingSeconds(final String token) {
        Date expiration = getClaims(token, accessTokenKey).getExpiration();
        return (expiration.getTime() - System.currentTimeMillis()) / 1000;
    }

    // ========================= 내부 메서드 =========================

    private CustomUserDetails getCustomUserDetails(final Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
    }

    private JwtBuilder getJwtBuilder() {
        return Jwts.builder().issuer(issuer);
    }

    private Claims getClaims(final String token, final SecretKey key) {
        try {
            return Jwts.parser()
                       .verifyWith(key)
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private boolean validateToken(final String token, final SecretKey key) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.", e);
        }
        return false;
    }

}
