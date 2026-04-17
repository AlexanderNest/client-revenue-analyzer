package ru.nesterov.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.nesterov.web.controller.request.PdfReportRequest;

@Tag(name = "Управление отчетами", description = "API для генерации и выгрузки документов статистики")
@RequestMapping("/reports")
public interface PdfReportController {

    @Operation(
            summary = "Сгенерировать PDF-отчет по клиенту",
            description = "Создает PDF-файл со статистикой встреч и доходов клиента за выбранный период времени. Параметры передаются в URL"
    )
    @GetMapping(value = "/client-statistic", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<StreamingResponseBody> generateReport(@RequestHeader("X-username") String username, PdfReportRequest request);

}

