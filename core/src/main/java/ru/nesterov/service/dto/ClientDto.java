package ru.nesterov.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientDto {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private boolean active;
}
