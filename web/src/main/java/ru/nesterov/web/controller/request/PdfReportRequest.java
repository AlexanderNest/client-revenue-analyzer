package ru.nesterov.web.controller.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PdfReportRequest {
    private String clientName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
