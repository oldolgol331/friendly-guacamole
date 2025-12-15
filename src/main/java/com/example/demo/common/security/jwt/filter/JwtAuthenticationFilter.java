package com.example.demo.common.security.jwt.filter;

import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_HEADER_NAME;
import static com.example.demo.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_PREFIX;
import static com.example.demo.infra.redis.constant.RedisConst.REDIS_ACCESS_TOKEN_BLACKLIST_PREFIX;

import com.example.demo.common.security.jwt.provider.JwtProvider;
import com.example.demo.infra.redis.dao.RedisRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * PackageName : com.example.demo.common.security.jwt.filter
 * FileName    : JwtAuthenticationFilter
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : JWT 인증 필터
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider     jwtProvider;
    private final RedisRepository redisRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        final String token = resolveToken(request);

        if (token != null
            && !redisRepository.hasKey(REDIS_ACCESS_TOKEN_BLACKLIST_PREFIX + token)
            && jwtProvider.validateAccessToken(token)) {
            Authentication authentication = jwtProvider.getAuthenticationFromAccessToken(token);

            if (authentication instanceof UsernamePasswordAuthenticationToken)
                ((UsernamePasswordAuthenticationToken) authentication).setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // ========================= 내부 메서드 =========================

    /**
     * 토큰을 추출합니다.
     *
     * @param request - HttpServletRequest
     * @return 토큰 값 또는 null
     */
    private String resolveToken(final HttpServletRequest request) {
        String bearerToken = request.getHeader(JWT_ACCESS_TOKEN_HEADER_NAME);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JWT_ACCESS_TOKEN_PREFIX))
            return bearerToken.substring(7);
        return null;
    }

}
