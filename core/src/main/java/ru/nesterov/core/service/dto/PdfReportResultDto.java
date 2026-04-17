package ru.nesterov.core.service.dto;

import lombok.Value;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Value
public class PdfReportResultDto {
    StreamingResponseBody responseBody;
    String fileName;
}
