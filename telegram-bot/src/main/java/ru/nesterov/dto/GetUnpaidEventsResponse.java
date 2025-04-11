package ru.nesterov.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GetUnpaidEventsResponse {
    private List<EventResponse> events;
}
