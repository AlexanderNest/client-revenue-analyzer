package ru.nesterov.dto;

import lombok.Data;

@Data
public class ClientResponse {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private boolean active;
}
