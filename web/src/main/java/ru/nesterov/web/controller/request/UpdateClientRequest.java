package ru.nesterov.web.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateClientRequest {
    @NotBlank(message = "Поле не может быть пустым")
    private String lastName;
    private String newName;
    private int pricePerHour;
    private String description;
    private boolean idGenerationNeeded;
    private String phone;
}
