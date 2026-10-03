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
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SecretTokenFilter implements Filter {
    private static final String SECRET_TOKEN = "X-secret-token";

    private final String token;
    private final boolean secretTokenEnabled;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private final List<String> excludedEndpointGroups = List.of(
            "/swagger-ui",
            "/v3/api-docs"
    );

    public SecretTokenFilter(@Value("${app.secret-token}") String token,
                             @Value("${app.secret-token.enabled}") boolean secretTokenEnabled) {
        this.token = token;
        this.secretTokenEnabled = secretTokenEnabled;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = urlPathHelper.getPathWithinApplication(request);

        if (isExcludedPath(path) || !secretTokenEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(SECRET_TOKEN);

        if (!token.equals(header)) {
            log.debug("Invalid secret token for, {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return excludedEndpointGroups.stream()
                .anyMatch(path::startsWith);
    }
}
