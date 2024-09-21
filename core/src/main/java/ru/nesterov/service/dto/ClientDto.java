package ru.nesterov.service.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
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
