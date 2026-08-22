package ru.nesterov.core.service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventUserActionDto {

    private String userId;
    private String message;

    @Override
    public String toString() {
        return "{'userId':'%s','message':'%s'}".formatted(userId, message);
    }
}
