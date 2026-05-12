package ru.nesterov.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import ru.nesterov.core.service.dto.CreatePdfReportDto;
import ru.nesterov.core.service.report.PdfReportService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
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
            CreatePdfReportDto dto = invocationOnMock.getArgument(0);
            dto.getOutputStream().write(fakePdfContent);
            dto.getOutputStream().flush();
            return null;
        }).when(pdfReportService).generateClientReportPdf(
                argThat(dto -> dto.getClientName().equals(clientName))
        );

        MvcResult result = mockMvc.perform(get("/reports/client-statistic")
                .header("X-username", username)
                .header("X-secret-token", "secret-token")
                .param("clientName", clientName)
                .param("startDate", "2026-01-01T00:00:00")
                .param("endDate", "2026-01-31T23:59:59"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"report_" + clientName)))
                .andReturn();

        byte[] responseContent = result.getResponse().getContentAsByteArray();
        assertNotNull(responseContent, "Массив байтов не должен быть null");
        assertTrue(responseContent.length > 0, "Содержимое ответа не должно быть пустым");
        assertArrayEquals(fakePdfContent, responseContent, "Содержимое ответа должно соответствовать данным, записанным моком");

    }

}
