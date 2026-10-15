package com.ampta.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized access error on {}: {}", request.getServletPath(), authException.getMessage());

        String acceptHeader = request.getHeader("Accept");
        boolean isBrowserRequest = (acceptHeader != null && acceptHeader.contains("text/html")) || !request.getServletPath().startsWith("/v1/");

        if (isBrowserRequest) {
            // Redirect web browser to login page
            response.sendRedirect(request.getContextPath() + "/login?unauthorized=true");
            return;
        }

        // Return JSON 401 for REST API clients
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(
                """
                {
                    "status": 401,
                    "error": "Unauthorized",
                    "message": "%s",
                    "path": "%s"
                }
                """.formatted(authException.getMessage(), request.getServletPath())
        );
    }
}
