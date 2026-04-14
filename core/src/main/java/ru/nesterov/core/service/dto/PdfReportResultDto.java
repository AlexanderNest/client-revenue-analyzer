package ru.nesterov.core.service.dto;

import lombok.Value;

import java.io.InputStream;

@Value
public class PdfReportResultDto {
    InputStream content;
    String fileName;
}
