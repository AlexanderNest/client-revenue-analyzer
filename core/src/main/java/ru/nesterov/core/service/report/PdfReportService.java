package ru.nesterov.core.service.report;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.core.service.dto.PdfReportDataDto;
import ru.nesterov.core.service.dto.PdfReportResultDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.event.EventService;
import ru.nesterov.core.service.event.EventsAnalyzerService;

import java.io.IOException;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfReportService {
    private final EventsAnalyzerService eventsAnalyzerService;
    private final EventService eventService;

    private static final String FONT_PATH = "/fonts/arial.ttf";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int TITTLE_FONT_SIZE = 18;
    private static final int BASE_FONT_SIZE = 14;
    private static final int NORMAL_FONT_SIZE = 12;

    private static final float TABLE_WIDTH_PERCENTAGE = 100f;
    private static final int[] COLUMN_WIDTHS = {3, 3, 2, 2};
    private static final int CELL_PADDING = 5;

    public PdfReportResultDto generateClientReportPdf(UserDto userDto, String clientName, LocalDateTime start, LocalDateTime end) {
        PdfReportDataDto reportData = eventsAnalyzerService.getReportData(userDto, clientName, start, end);
        String fileName = String.format("report_%s_%s.pdf", clientName, LocalDate.now());
        StreamingResponseBody responseBody =  outputStream -> {
            renderPdfToStream(reportData, outputStream);
        };
        return new PdfReportResultDto(responseBody, fileName);
    }

    private void renderPdfToStream(PdfReportDataDto dataDto, OutputStream outputStream) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            BaseFont baseF = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseF, TITTLE_FONT_SIZE, Font.BOLD);
            Font baseFont = new Font(baseF, BASE_FONT_SIZE, Font.BOLD);
            Font normalFont = new Font(baseF, NORMAL_FONT_SIZE, Font.NORMAL);

            Paragraph title = new Paragraph("Отчет по клиенту: " + dataDto.getStats().getName(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph period = new Paragraph("за период с " + dataDto.getStart().format(DATE_FORMATTER) + " по " + dataDto.getEnd().format(DATE_FORMATTER), baseFont);
            period.setAlignment(Element.ALIGN_CENTER);
            document.add(period);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Статистика за указанный период: ", normalFont));
            document.add(new Paragraph("Доход: " + (long) dataDto.getStats().getActualIncome() + " руб.", normalFont));
            document.add(new Paragraph("Часов отработано: " + dataDto.getStats().getSuccessfulMeetingsHours(), normalFont));
            document.add(new Paragraph("Всего встреч: " + dataDto.getStats().getSuccessfulEventsCount(), normalFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(TABLE_WIDTH_PERCENTAGE);
            table.setWidths(COLUMN_WIDTHS);

            addTableCell(table, "Дата начала", baseFont);
            addTableCell(table, "Дата окончания", baseFont);
            addTableCell(table, "Статус", baseFont);
            addTableCell(table, "Доход", baseFont);

            for (EventDto event : dataDto.getEvents()) {
                addTableCell(table, event.getStart().format(DATE_FORMATTER), normalFont);
                addTableCell(table, event.getEnd().format(DATE_FORMATTER), normalFont);
                addTableCell(table, event.getStatus().toString(), normalFont);
                addTableCell(table, String.format("%.0f", eventService.getEventIncome(dataDto.getClient(), event)), normalFont);
            }
            document.add(table);
            document.close();
        } catch (DocumentException | IOException e) {
            log.error("Ошибка при генерации PDF отчета", e);
            throw new RuntimeException("Не удалось создать PDF отчет",e);
        }
    }

    private void addTableCell(PdfPTable table, String title, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(CELL_PADDING);
        table.addCell(cell);
    }
}
