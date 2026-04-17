package ru.nesterov.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.nesterov.core.service.dto.PdfReportResultDto;
import ru.nesterov.core.service.report.PdfReportService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReportControllerTest extends AbstractControllerTest {

    @MockitoBean
    private PdfReportService pdfReportService;

    @Test
    void generateClientReportShouldReturnPdfStream() throws Exception {
        String username = "testUser";
        createUser(username);

        PdfReportResultDto mockResult = new PdfReportResultDto(
                outputStream -> outputStream.write("fake pdf".getBytes()),
                "test_report.pdf"
        );
        when(pdfReportService.generateClientReportPdf(any(), any(), any(), any())).thenReturn(mockResult);

        mockMvc.perform(get("/reports/client-statistic")
                .header("X-username", username)
                .header("X-secret-token", "secret-token")
                .param("clientName", "Ivan")
                .param("startDate", "2026-01-01T00:00:00")
                .param("endDate", "2026-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test_report.pdf\""));
    }

}
