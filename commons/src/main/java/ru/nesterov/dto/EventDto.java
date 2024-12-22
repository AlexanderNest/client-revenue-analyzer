package ru.nesterov.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Builder
@Getter
public class EventDto {
    private EventStatus status;
    private String summary;
    private LocalDateTime start;
    private LocalDateTime end;
    private EventExtensionDto eventExtensionDto;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventDto second) {
            return this.status == second.status && this.summary.equals(second.summary) && this.start.equals(second.start) && this.end.equals(second.end);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, summary, start, end);
    }
}

