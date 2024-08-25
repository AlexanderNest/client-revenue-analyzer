package ru.nesterov.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientDto {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private boolean active;
}
