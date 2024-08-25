package ru.nesterov.controller.response;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
public class GetFilteredClientsResponse {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private boolean active;
}
