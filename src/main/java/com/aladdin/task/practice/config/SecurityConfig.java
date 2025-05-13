package com.aladdin.task.practice.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; // AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 필터 순서 정의용


import com.aladdin.task.practice.utils.jwt.JwtAuthenticationEntryPoint;
import com.aladdin.task.practice.utils.jwt.JwtAuthenticationFilter;
import com.aladdin.task.practice.utils.jwt.JwtTokenProvider;

@Configuration
@EnableWebSecurity // Spring Security 활성화
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // EntryPoint 주입
    private final JwtTokenProvider jwtTokenProvider; // JWT Provider 주입
    private final UserDetailsService userDetailsService; // UserDetailsService 주입

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtTokenProvider jwtTokenProvider,
                          UserDetailsService userDetailsService) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    // PasswordEncoder 빈은 AppConfig에서 이미 등록했다고 가정합니다.
    // @Bean
    // public PasswordEncoder passwordEncoder() { ... }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // CSRF 비활성화 (메소드 체인 방식)
            .httpBasic().disable() // HTTP Basic 비활성화
            .formLogin().disable() // Form Login 비활성화

            // 세션 사용 비활성화
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and() // 이전 설정으로 돌아가 다른 설정 시작

            // 예외 처리 설정
            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and() // 이전 설정으로 돌아가 다른 설정 시작

            // 요청별 인가 규칙 설정 (authorizeRequests() 및 antMatchers 사용)
            .authorizeRequests() // <- authorizeHttpRequests 대신 authorizeRequests 사용
                // 회원 가입 및 로그인 경로는 인증 없이 허용 (antMatchers 사용)
                .antMatchers("/users/signup", "/users/login").permitAll()
                // .antMatchers(HttpMethod.POST, "/users/signup", "/users/login").permitAll() // 메소드 명시도 가능

                // 내 정보 조회 경로는 인증 필요 (authenticated)
                .antMatchers("/users/me").authenticated() // 모든 HTTP 메소드에 적용
                // 만약 특정 메소드만 필요하다면 아래와 같이 사용
                // .antMatchers(HttpMethod.GET, "/users/me").authenticated()
                // .antMatchers(HttpMethod.PUT, "/users/me").authenticated()
                // .antMatchers(HttpMethod.DELETE, "/users/me").authenticated()

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
                .and() // 이전 설정으로 돌아가 다른 설정 시작

            // JWT 인증 필터를 Spring Security 필터 체인에 추가
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        // 최종 SecurityFilterChain 빌드 및 반환
        return http.build();
    }
}
