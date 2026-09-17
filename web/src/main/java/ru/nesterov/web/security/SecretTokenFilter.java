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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SecretTokenFilter implements Filter {
    private static final String SECRET_TOKEN = "X-secret-token";
    private static final String USERNAME_HEADER = "X-username";

    private final String token;
    private final boolean secretTokenEnabled;
    private final UserDetailsService userDetailsService;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private final List<String> excludedEndpointGroups = List.of(
            "/swagger-ui",
            "/v3/api-docs"
    );

    private final List<String> endpointsWithoutUsername = List.of(
            "/user/createUser",
            "/user/getUserByUsername"
    );

    public SecretTokenFilter(@Value("${app.secret-token}") String token,
                             @Value("${app.secret-token.enabled}") boolean secretTokenEnabled, UserDetailsService userDetailsService) {
        this.token = token;
        this.secretTokenEnabled = secretTokenEnabled;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = urlPathHelper.getPathWithinApplication(request);

        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (secretTokenEnabled) {
            String header = request.getHeader(SECRET_TOKEN);

            if (!token.equals(header)) {
                log.debug("Invalid secret token for, {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        if (isEndpointWithoutUsername(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = request.getHeader(USERNAME_HEADER);

        if (username == null || username.isBlank()) {
            log.debug("Username header is missing");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (UsernameNotFoundException e) {
            log.debug("User not found: {}", username);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return excludedEndpointGroups.stream()
                .anyMatch(path::startsWith);
    }

    private boolean isEndpointWithoutUsername(String path) {
        return endpointsWithoutUsername.stream()
                .anyMatch(path::startsWith);
    }
}
