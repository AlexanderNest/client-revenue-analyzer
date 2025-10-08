package ru.nesterov.web.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateClientRequest {
    @NotBlank(message = "Поле не может быть пустым")
    private String clientName;
    private String newName;
    private Integer pricePerHour;
    private String description;
    private Boolean idGenerationNeeded;
    private String phone;
}
