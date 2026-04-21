package ru.nesterov.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import ru.nesterov.core.service.report.PdfReportService;

import java.io.OutputStream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReportControllerTest extends AbstractControllerTest {

    @MockitoBean
    private PdfReportService pdfReportService;

    @Test
    void generateClientReportShouldReturnPdfStream() throws Exception {
        String username = "genClRprtRetPdfStrm";
        createUser(username);
        String clientName = "Ivan";
        byte[] fakePdfContent = "fake pdf content".getBytes();

        doAnswer(invocationOnMock -> {
            OutputStream outputStream = invocationOnMock.getArgument(4);
            outputStream.write(fakePdfContent);
            return null;
        }).when(pdfReportService).generateClientReportPdf(
                any(),
                eq(clientName),
                any(),
                any(),
                any(OutputStream.class)
        );

        mockMvc.perform(get("/reports/client-statistic")
                .header("X-username", username)
                .header("X-secret-token", "secret-token")
                .param("clientName", clientName)
                .param("startDate", "2026-01-01T00:00:00")
                .param("endDate", "2026-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"report_" + clientName)))
                .andReturn();

    }

}
