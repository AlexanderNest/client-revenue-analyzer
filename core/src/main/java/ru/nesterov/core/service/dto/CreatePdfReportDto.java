package ru.nesterov.core.service.dto;

import lombok.Value;

import java.io.OutputStream;
import java.time.LocalDateTime;

@Value
public class CreatePdfReportDto {
    UserDto userDto;
    String clientName;
    java.time.LocalDateTime start;
    LocalDateTime end;
    OutputStream outputStream;
}
