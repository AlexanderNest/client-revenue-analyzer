package ru.nesterov.web.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GetForYearAndMonthRequest {
    @NotBlank
    private String monthName;

    private Integer year = LocalDate.now().getYear();
}
