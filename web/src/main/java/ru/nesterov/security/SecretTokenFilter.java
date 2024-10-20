package ru.nesterov.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecretTokenFilter extends OncePerRequestFilter {
    private static final String HEADER = "X-secret-token";

    private final String token;
    private final boolean secretTokenEnabled;


    public SecretTokenFilter(@Value("${app.secret-token}") String token, @Value("${app.secret-token.enabled}") boolean secretTokenEnabled) {
        this.token = token;
        this.secretTokenEnabled = secretTokenEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HEADER);

        if (secretTokenEnabled && (token == null || !token.equals(this.token))) {
            // Если токена нет, возвращаем 403 Forbidden
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Missing or invalid token");
            return;
        }

        // Продолжаем цепочку фильтров, если токен найден
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Исключаем из фильтрации маршруты для Swagger
        String path = request.getRequestURI();
        return path.contains("swagger-ui")
                || path.contains("api-docs")
                || path.contains("swagger-resources");
    }

}
