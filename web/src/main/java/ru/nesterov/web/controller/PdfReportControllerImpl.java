package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.nesterov.core.service.dto.CreatePdfReportDto;
import ru.nesterov.core.service.report.PdfReportService;
import ru.nesterov.core.service.user.UserService;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.web.controller.request.PdfReportRequest;

import java.io.OutputStream;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class PdfReportControllerImpl implements PdfReportController {
    private final PdfReportService pdfReportService;
    private final UserService userService;

    @Override
    public ResponseEntity<StreamingResponseBody> generateReport(@RequestHeader("X-username") String username, PdfReportRequest request) {
        StreamingResponseBody responseBody = outputStream -> pdfReportService.generateClientReportPdf(
                buildCreatePdfReportDto(username, request, outputStream)
        );

        String fileName = String.format("report_%s_%s.pdf", request.getClientName(), LocalDate.now());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(responseBody);
    }

    private CreatePdfReportDto buildCreatePdfReportDto(String username, PdfReportRequest request, OutputStream outputStream) {
        UserDto userDto = userService.getUserByUsername(username);
        return new CreatePdfReportDto(
                userDto,
                request.getClientName(),
                request.getStartDate(),
                request.getEndDate(),
                outputStream
        );
    }
}
