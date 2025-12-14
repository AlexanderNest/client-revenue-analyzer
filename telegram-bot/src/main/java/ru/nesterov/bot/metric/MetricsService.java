package ru.nesterov.bot.metric;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry meterRegistry;

    public void recordHandlerCall(
            String handler,
            long durationMs,
            boolean success,
            Exception exception
    ) {

        if (success) {
            meterRegistry.counter("bot_handler_success", "handler", handler)
                    .increment();
        } else {
            meterRegistry.counter(
                    "bot_handler_error",
                    "handler", handler,
                    "exception", exception != null ? exception.getClass().getSimpleName() : "Unknown"
            ).increment();
        }

        meterRegistry.timer("bot_handler_duration", "handler", handler)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
