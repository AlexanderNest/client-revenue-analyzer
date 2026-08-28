package ru.nesterov.web.controller.request.client;

import lombok.Data;

@Data
public class CreateClientRequest {
    private String name;
    private int pricePerHour;
    private String description;
    private boolean idGenerationNeeded;
    private String phone;
}
