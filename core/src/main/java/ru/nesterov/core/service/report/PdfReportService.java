package ru.nesterov.core.service.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.event.EventService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PdfReportService {

    private static final String FONT_PATH = "/fonts/arial.ttf";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final EventService eventService;

    public PdfReportService(EventService eventService) {
        this.eventService = eventService;
    }

    public byte[] generateClientReportPdf(ClientMeetingsStatistic stats, List<EventDto> events, Client client, LocalDateTime start, LocalDateTime end) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(bf, 18, Font.BOLD);
            Font baseFont = new Font(bf, 14, Font.BOLD);
            Font normalFont = new Font(bf, 12, Font.NORMAL);

            Paragraph title = new Paragraph("Отчет по клиенту: " + stats.getName(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph period = new Paragraph("за период с " + start.format(dtf) + " по " + end.format(dtf), baseFont);
            period.setAlignment(Element.ALIGN_CENTER);
            document.add(period);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Статистика за указанный период: ", normalFont));
            document.add(new Paragraph("Доход: " + (long) stats.getActualIncome() + " руб.", normalFont));
            document.add(new Paragraph("Часов отработано: " + stats.getSuccessfulMeetingsHours(), normalFont));
            document.add(new Paragraph("Всего встреч: " + stats.getSuccessfulEventsCount(), normalFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{3, 3, 2, 2});

            addTableCell(table, "Дата начала", baseFont);
            addTableCell(table, "Дата окончания", baseFont);
            addTableCell(table, "Статус", baseFont);
            addTableCell(table, "Доход", baseFont);

            for (EventDto event : events) {
                addTableCell(table, event.getStart().format(dtf), normalFont);
                addTableCell(table, event.getEnd().format(dtf), normalFont);
                addTableCell(table, event.getStatus().toString(), normalFont);
                addTableCell(table, String.format("%.0f", eventService.getEventIncome(client, event)), normalFont);
            }
            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Ошибка при генерации PDF отчета", e);
            throw new RuntimeException("Не удалось создать PDF отчет",e);
        }
    }

    private void addTableCell(PdfPTable table, String title, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
