package ru.nesterov.web.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateClientResponse {
    private String newName;
    private int pricePerHour;
    private String description;
    private boolean idGenerationNeeded;
    private String phone;
}
