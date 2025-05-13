package com.aladdin.task.practice.utils.jwt;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException; // 인증 예외
import org.springframework.security.web.AuthenticationEntryPoint; // EntryPoint 인터페이스
import org.springframework.stereotype.Component;

@Component // Spring 빈으로 등록
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // 인증되지 않은 사용자 접근 시 호출되는 메소드
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 401 Unauthorized 상태 코드 반환
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        // 응답 본문에 JSON 에러 메시지 포함 (선택 사항)
        response.setContentType("application/json");
        // authException.getMessage()는 "Full authentication is required to access this resource" 등이 될 수 있습니다.
        // 만료 예외는 필터에서 잡았으므로 여기서는 다른 인증 실패 메시지를 제공할 수 있습니다.
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
        response.getWriter().flush();
    }
}
