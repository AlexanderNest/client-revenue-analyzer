package ru.nesterov.core.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateClientDto {
    private String newName;
    private Integer pricePerHour;
    private String description;
    private Boolean idGenerationNeeded;
    private String phone;
}
