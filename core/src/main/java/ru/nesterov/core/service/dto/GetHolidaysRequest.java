package ru.nesterov.core.service.dto;

import lombok.Data;

@Data
public class GetHolidaysRequest {
    private String leftDateStr;
    private String rightDateStr;
}
