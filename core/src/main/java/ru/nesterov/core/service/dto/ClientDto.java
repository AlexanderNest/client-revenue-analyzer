package ru.nesterov.core.service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ClientDto {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private boolean active;
    private Date startDate;
    private String phone;
}
