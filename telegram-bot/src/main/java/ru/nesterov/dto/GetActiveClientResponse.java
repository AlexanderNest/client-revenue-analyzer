package ru.nesterov.dto;

import lombok.Data;

@Data
public class GetActiveClientResponse {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private boolean active;

    @Override
    public String toString() {
        return "Клиент " +
                "по имени: " + name +
                ", идентификационный номер: " + id + '\'' +
                ", цена за час: " + pricePerHour + '\n';
    }
}
