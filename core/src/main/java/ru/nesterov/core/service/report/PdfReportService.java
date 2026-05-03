package ru.nesterov.core.service.report;

import org.openpdf.text.Document;
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
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventsFilter;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.exception.CannotCreatePDFReportException;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.dto.CreatePdfReportDto;
import ru.nesterov.core.service.event.EventService;
import ru.nesterov.core.service.event.EventsAnalyzerService;

import java.io.IOException;
import java.util.List;

import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfReportService {
    private final EventsAnalyzerService eventsAnalyzerService;
    private final EventService eventService;
    private final CalendarService calendarService;
    private final ClientRepository clientRepository;

    private static final String FONT_PATH = "/fonts/arial.ttf";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int TITTLE_FONT_SIZE = 18;
    private static final int BASE_FONT_SIZE = 14;
    private static final int NORMAL_FONT_SIZE = 12;

    private static final float TABLE_WIDTH_PERCENTAGE = 100f;
    private static final int[] COLUMN_WIDTHS = {3, 3, 2, 2};
    private static final int CELL_PADDING = 5;

    public void generateClientReportPdf(CreatePdfReportDto createPdfReportDto) {

        try {
            createPdfAndRenderToStream(createPdfReportDto, createPdfReportDto.getOutputStream());
        } catch (Exception e) {
            log.error("Ошибка во время генерации отчета. User = [{}], client = [{}]",
                    createPdfReportDto.getUserDto().getUsername(), createPdfReportDto.getClientName(), e);

            throw new CannotCreatePDFReportException(e.getMessage());
        }
    }

    private void createPdfAndRenderToStream(CreatePdfReportDto reportDto, OutputStream outputStream) throws IOException {
        BaseFont baseF = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Font titleFont = new Font(baseF, TITTLE_FONT_SIZE, Font.BOLD);
        Font baseFont = new Font(baseF, BASE_FONT_SIZE, Font.BOLD);
        Font normalFont = new Font(baseF, NORMAL_FONT_SIZE, Font.BOLD);
        Document document = new Document(PageSize.A4);

        PdfWriter.getInstance(document, outputStream);
        document.open();
        fillDocumentContent(document, reportDto, titleFont, baseFont, normalFont);
        document.close();
    }

    private void fillDocumentContent(Document document, CreatePdfReportDto reportDto, Font titleFont, Font baseFont, Font normalFont) {
        ClientMeetingsStatistic statistics = eventsAnalyzerService.getStatisticByClientMeetingsBetweenDates(reportDto.getUserDto(), reportDto.getClientName(), reportDto.getStart(), reportDto.getEnd());

        Paragraph title = new Paragraph("Отчет по клиенту: " + reportDto.getClientName(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph period = new Paragraph("за период с " + reportDto.getStart().format(DATE_FORMATTER) + " по " + reportDto.getEnd().format(DATE_FORMATTER), baseFont);
        period.setAlignment(Element.ALIGN_CENTER);
        document.add(period);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Статистика за указанный период: ", normalFont));
        document.add(new Paragraph("Доход: " + (long) statistics.getActualIncome() + " руб.", normalFont));
        document.add(new Paragraph("Часов отработано: " + statistics.getSuccessfulMeetingsHours(), normalFont));
        document.add(new Paragraph("Всего встреч: " + statistics.getSuccessfulEventsCount(), normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(TABLE_WIDTH_PERCENTAGE);
        table.setWidths(COLUMN_WIDTHS);

        addTableCell(table, "Дата начала", baseFont);
        addTableCell(table, "Дата окончания", baseFont);
        addTableCell(table, "Статус", baseFont);
        addTableCell(table, "Доход", baseFont);

        EventsFilter eventsFilter = EventsFilter.builder()
                .mainCalendar(reportDto.getUserDto().getMainCalendar())
                .cancelledCalendar(reportDto.getUserDto().getCancelledCalendar())
                .isCancelledCalendarEnabled(reportDto.getUserDto().isCancelledCalendarEnabled())
                .clientName(reportDto.getClientName())
                .leftDate(reportDto.getStart())
                .rightDate(reportDto.getEnd())
                .build();

        List<EventDto> eventDtoList = calendarService.getEventsBetweenDates(eventsFilter);
        Client client = clientRepository.findClientByNameAndUserId(reportDto.getClientName(), reportDto.getUserDto().getId());

        for (EventDto event : eventDtoList) {
            addTableCell(table, event.getStart().format(DATE_FORMATTER), normalFont);
            addTableCell(table, event.getEnd().format(DATE_FORMATTER), normalFont);
            addTableCell(table, event.getStatus().toString(), normalFont);
            addTableCell(table, String.format("%.0f", eventService.getEventIncome(client, event)), normalFont);
        }
        document.add(table);
    }

    private void addTableCell(PdfPTable table, String title, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(CELL_PADDING);
        table.addCell(cell);
    }
}
