package ru.nesterov.config.filter.chain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

@Component
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

        // Буфер для формирования лога
        StringBuilder logMessage = new StringBuilder();

        // Логируем информацию о запросе
        logMessage.append("\n=== HTTP Request ===\n")
                .append("Method: ").append(wrappedRequest.getMethod()).append("\n")
                .append("URI: ").append(wrappedRequest.getRequestURI()).append("\n")
                .append("Headers:\n").append(formatHeaders(wrappedRequest)).append("\n")
                .append("Parameters:\n").append(formatParameters(wrappedRequest.getParameterMap())).append("\n")
                .append("Body:\n").append(wrappedRequest.getCachedBody()).append("\n");

        // Логируем запрос
        logger.info(logMessage.toString());

        // Продолжаем выполнение цепочки фильтров с обернутым запросом
        filterChain.doFilter(wrappedRequest, response);

        // Логируем информацию об ответе
        logMessage.setLength(0); // Очищаем StringBuilder для ответа
        logMessage.append("\n=== HTTP Response ===\n")
                .append("Status: ").append(response.getStatus()).append("\n");

        logger.info(logMessage.toString());
    }

    private String formatHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append("  ").append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
        }
        return headers.toString();
    }

    private String formatParameters(Map<String, String[]> parameterMap) {
        if (parameterMap.isEmpty()) {
            return "  No parameters";
        }

        StringBuilder params = new StringBuilder();
        parameterMap.forEach((key, values) -> {
            params.append("  ").append(key).append("=");
            if (values.length == 1) {
                params.append(values[0]);
            } else {
                params.append("[");
                params.append(String.join(", ", values));
                params.append("]");
            }
            params.append("\n");
        });
        return params.toString().trim();
    }
}