package ru.nesterov.bot.handlers.implementation.invocable.stateful.updateClient;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateClientResponse {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private boolean active;
    private Date startDate;
    private String phone;
}
