package ru.nesterov.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecretTokenFilter implements Filter {
    private static final String HEADER = "X-secret-token";

    private final String token;
    private final boolean secretTokenEnabled;


    public SecretTokenFilter(@Value("${app.secret-token}") String token, @Value("${app.secret-token.enabled}") boolean secretTokenEnabled) {
        this.token = token;
        this.secretTokenEnabled = secretTokenEnabled;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!secretTokenEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        String header = request.getHeader(HEADER);
        if (!token.equals(header)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
