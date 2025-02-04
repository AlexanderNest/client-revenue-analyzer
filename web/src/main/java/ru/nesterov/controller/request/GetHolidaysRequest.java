package ru.nesterov.controller.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class GetHolidaysRequest {
    private String leftDateStr;
    private String rightDateStr;
}
