package ru.nesterov.web.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SecretTokenFilter implements Filter {
    private static final String HEADER = "X-secret-token";

    private final String token;
    private final boolean secretTokenEnabled;

    private final List<String> excludedEndpointGroups = List.of(
            "/swagger-ui",
            "/api-docs",
            "/h2-console"
    );

    public SecretTokenFilter(@Value("${app.secret-token}") String token, @Value("${app.secret-token.enabled}") boolean secretTokenEnabled) {
        this.token = token;
        this.secretTokenEnabled = secretTokenEnabled;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!secretTokenEnabled || isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HEADER);
        if (!token.equals(header)) {
            log.debug("Invalid secret token for, {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isExcludedPath(String path) {
        return excludedEndpointGroups.stream()
                .anyMatch(path::contains);
    }
}
