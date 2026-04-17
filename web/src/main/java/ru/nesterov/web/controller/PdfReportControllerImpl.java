package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.nesterov.core.service.dto.PdfReportResultDto;
import ru.nesterov.core.service.report.PdfReportService;
import ru.nesterov.core.service.user.UserService;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.web.controller.request.PdfReportRequest;

@RestController
@RequiredArgsConstructor
public class PdfReportControllerImpl implements PdfReportController {
    private final PdfReportService pdfReportService;
    private final UserService userService;

    @Override
    public ResponseEntity<StreamingResponseBody> generateReport(@RequestHeader("X-username") String username, PdfReportRequest request) {
        UserDto userDto = userService.getUserByUsername(username);

        PdfReportResultDto reportDto = pdfReportService.generateClientReportPdf(
                userDto,
                request.getClientName(),
                request.getStartDate(),
                request.getEndDate()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reportDto.getFileName() + "\"")
                .body(reportDto.getResponseBody());
    }
}
