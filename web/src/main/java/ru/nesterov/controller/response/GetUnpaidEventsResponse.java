package ru.nesterov.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class GetUnpaidEventsResponse {
    private List<EventResponse> events;
}
