package ru.nesterov.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateClientRequest {
    private String name;
    private Integer pricePerHour;
    private String description;
    private Boolean idGenerationNeeded;
    private String phone;

    public boolean isFilled() {
        return name != null
                && pricePerHour != null
                && description != null
                && idGenerationNeeded != null
                && phone != null;
    }
}
