package ru.nesterov.web.config.filter.chain;

import io.micrometer.tracing.Baggage;
import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
public class ClientIdBaggageFilter implements Filter {
    private final Tracer tracer;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String username = httpRequest.getHeader("x-username");
        if (username == null || username.isBlank()) {
            username = "unknown";
        }

        Span currentSpan = tracer.currentSpan();
        BaggageInScope baggageInScope = null;

        if (currentSpan != null) {
            Baggage baggage = tracer.getBaggage("x-username");
            baggage.set(username);
            baggageInScope = baggage.makeCurrent();
            MDC.put("x-username", username);
        } else {
            MDC.put("x-username", username);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            if (baggageInScope != null) {
                baggageInScope.close();
            }

            MDC.remove("x-username");
        }
    }
}