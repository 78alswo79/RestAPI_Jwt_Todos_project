package com.aladdin.task.practice.utils.jwt;


import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 인증 객체 타입
import org.springframework.security.core.context.SecurityContextHolder; // SecurityContext 접근
import org.springframework.security.core.userdetails.UserDetails; // UserDetails 타입
import org.springframework.security.core.userdetails.UserDetailsService; // 사용자 정보 로딩 서비스
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 사용자 없음 예외
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException; // 만료 예외

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService; // UserDetailsService 주입

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request); // 요청에서 토큰 추출

        // 토큰이 존재하고 현재 Security Context에 인증 정보가 없는 경우에만 처리
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 1. 토큰 유효성 검사 (만료 시 validateToken에서 ExpiredJwtException 발생)
                jwtTokenProvider.validateToken(token); // validateToken에서 예외 발생 가능

                // 2. 토큰에서 사용자 이름 추출
                String username = jwtTokenProvider.getUsernameFromToken(token);

                // 3. UserDetailsService를 사용하여 사용자 정보(UserDetails) 로드
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 4. UserDetails와 토큰을 기반으로 인증 객체 (Authentication) 생성
                // 인증된 상태임을 나타내기 위해 password는 null, authorities 전달
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 5. 생성된 인증 객체를 Security Context에 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                 // JWT 토큰 만료 예외는 SecurityConfig의 EntryPoint에서 처리하도록 예외를 다시 던지거나,
                 // 또는 여기서 HttpServletResponse를 사용하여 401 응답을 직접 작성할 수도 있습니다.
                 // 여기서는 SecurityConfig의 EntryPoint로 넘어가도록 처리하거나, EntryPoint 없이 바로 응답을 작성합니다.
                 // SecurityConfig의 exceptionHandling().authenticationEntryPoint()를 사용한다면 여기서 별도 처리 불필요.
                 // EntryPoint를 사용하지 않는다면 여기서 직접 401 응답 작성 (이전 'Security 없이 구현' 방식과 유사)
                 // 여기서는 EntryPoint를 사용할 것이므로 별도 처리 없이 진행. (필터 체인 계속 진행 시 EntryPoint 도달)

                 // 필터 체인을 중단하고 EntryPoint로 이동시키기 위해 예외를 다시 던지거나,
                 // 또는 SecurityContextHolder.clearContext(); 하고 다음 필터로 넘어가도록 할 수 있습니다.
                 // 일반적으로는 EntryPoint에서 처리하도록 합니다.
                 // throw e; // <-- EntryPoint로 넘어가도록 예외 다시 던지기 (필요시)

                 // 만약 필터에서 직접 응답을 작성하려면:
                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                 response.setContentType("application/json");
                 response.getWriter().write("{\"error\": \"JWT token expired\"}");
                 response.getWriter().flush();
                 return; // 필터 체인 중단

            } catch (UsernameNotFoundException e) {
                // 토큰의 사용자 이름에 해당하는 사용자가 DB에 없는 경우
                 response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
                 response.setContentType("application/json");
                 response.getWriter().write("{\"error\": \"User not found from token\"}");
                 response.getWriter().flush();
                 return; // 필터 체인 중단

            } catch (Exception e) {
                // 다른 JWT 유효성 검사 실패 (잘못된 서명, 형식 등)
                 System.err.println("JWT validation failed: " + e.getMessage());
                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                 response.setContentType("application/json");
                 response.getWriter().write("{\"error\": \"Invalid JWT token\"}");
                 response.getWriter().flush();
                 return; // 필터 체인 중단
            }
        }

        // Security Context에 인증 정보가 설정되었거나, 토큰이 없거나, 예외 처리로 응답이 작성되지 않았다면 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}
