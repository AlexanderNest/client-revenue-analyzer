package ru.nesterov.controller.request;

import lombok.Data;

@Data
public class GetHolidaysRequest {
    private String leftDateStr;
    private String rightDateStr;
}
