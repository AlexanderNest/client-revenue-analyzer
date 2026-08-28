package ru.nesterov.calendar.integration.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nesterov.calendar.integration.dto.CalendarType;
import ru.nesterov.calendar.integration.dto.CreateEventDto;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventExtensionDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.PrimaryEventData;
import ru.nesterov.calendar.integration.dto.ResponseCreateEventDto;
import ru.nesterov.calendar.integration.exception.CalendarIntegrationException;
import ru.nesterov.calendar.integration.exception.CannotBuildEventIntegrationException;
import ru.nesterov.calendar.integration.service.CalendarClient;
import ru.nesterov.calendar.integration.service.EventStatusService;
import ru.nesterov.calendar.integration.util.PlainTextMapper;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/*
Из полезного: есть shared и private extended properties. Там можно хранить метаданные

Основные методы Google Calendar API для работы с событиями:

1. delete(String calendarId, String eventId)
   - Удаляет событие из календаря.

2. get(String calendarId, String eventId)
   - Получает информацию о конкретном событии.

3. importEvent(String calendarId, Event event)
   - Импортирует событие в календарь без отправки приглашений.

4. insert(String calendarId, Event event)
   - Создаёт новое событие в календаре.

5. instances(String calendarId, String eventId)
   - Возвращает список экземпляров повторяющегося события.

6. list(String calendarId)
   - Получает список событий с возможностью фильтрации и сортировки.

7. move(String calendarId, String eventId, String destinationCalendarId)
   - Перемещает событие в другой календарь.

8. patch(String calendarId, String eventId, Event event)
   - Частично обновляет событие (обновляет только указанные поля).

9. update(String calendarId, String eventId, Event event)
   - Полностью обновляет событие.

10. watch(String calendarId, Channel channel)
    - Устанавливает уведомления об изменении событий в календаре.
*/

@Slf4j
@Component
@ConditionalOnProperty("app.google.calendar.integration.enabled")
public class GoogleCalendarClient implements CalendarClient {
    private static final String DEFAULT_EVENT_TYPE = "default";
    /**
     * Максимальное количество запросов в одном батче Google Calendar API
     */
    private static final int MAX_BATCH_SIZE = 50;
    private static final int EVENT_NOT_FOUND_CODE = 404;
    private static final int EVENT_ALREADY_DELETED_CODE = 410;

    private final Calendar calendar;
    private final GoogleCalendarProperties properties;
    private final ObjectMapper objectMapper;
    private final EventStatusService eventStatusService;

    public GoogleCalendarClient(GoogleCalendarProperties properties, ObjectMapper objectMapper,
                                EventStatusService eventStatusService) throws GeneralSecurityException, IOException {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.eventStatusService = eventStatusService;
        this.calendar = createCalendarService();
    }

    private Calendar createCalendarService() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(properties.getServiceAccountFilePath()))
                .createScoped(List.of(CalendarScopes.CALENDAR));

        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName(properties.getApplicationName())
                .build();
    }

    public List<EventDto> getEventsBetweenDates(String calendarId, CalendarType calendarType, LocalDateTime leftDate, LocalDateTime rightDate) {
        return getEventsBetweenDatesInternal(calendarId, calendarType, leftDate, rightDate, null);
    }

    public List<EventDto> getEventsBetweenDates(String calendarId, CalendarType calendarType, LocalDateTime leftDate, LocalDateTime rightDate, String clientName) {
        return getEventsBetweenDatesInternal(calendarId, calendarType, leftDate, rightDate, clientName);
    }

    @SneakyThrows
    @Override
    public List<ResponseCreateEventDto> createEvents(String calendarId, List<CreateEventDto> createEventDtoList) {
        List<ResponseCreateEventDto> responseList = new ArrayList<>();
        List<GoogleJsonError> errors = new ArrayList<>();

        for (List<CreateEventDto> eventsChunk : partition(createEventDtoList, MAX_BATCH_SIZE)) {
            BatchRequest batchRequest = calendar.batch(httpRequest -> httpRequest.setReadTimeout(3 * 60000));

            for (CreateEventDto eventToCreate : eventsChunk) {
                JsonBatchCallback<Event> callback = new JsonBatchCallback<>() {
                    @Override
                    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
                        log.error("Ошибка создания события {}", e);
                        errors.add(e);
                    }

                    @Override
                    public void onSuccess(Event createdEvent, HttpHeaders responseHeaders) {
                        log.debug("Было создано событие c id: {}", createdEvent.getId());
                        responseList.add(ResponseCreateEventDto.builder()
                                .eventId(createdEvent.getId())
                                .clientId(eventToCreate.getClientId())
                                .build());
                    }
                };

                String colorId = eventStatusService.getColorId(eventToCreate.getStatus());

                Event newEvent = new Event()
                        .setSummary(eventToCreate.getSummary())
                        .setDescription(eventToCreate.getDescription())
                        .setColorId(colorId)
                        .setStart(new EventDateTime().setDateTime(new DateTime(eventToCreate.getStart())))
                        .setEnd(new EventDateTime().setDateTime(new DateTime(eventToCreate.getEnd())));

                calendar.events().insert(calendarId, newEvent).queue(batchRequest, callback);
            }

            batchRequest.execute();

            if (!errors.isEmpty()) {
                break;
            }
        }

        if (!errors.isEmpty()) {
            rollbackCreatedEvents(calendarId, responseList);
            throw new CalendarIntegrationException("Создание событий завершилось с ошибкой");
        }

        return responseList;
    }

    @SneakyThrows
    @Override
    public void deleteEvents(String calendarId, List<String> eventIdList) {
        if (eventIdList == null || eventIdList.isEmpty()) {
            log.debug("Список событий для удаления пуст");
            return;
        }

        List<GoogleJsonError> errors = new ArrayList<>();

        for (List<String> eventIdsChunk : partition(eventIdList, MAX_BATCH_SIZE)) {
            BatchRequest batchRequest = calendar.batch(httpRequest -> httpRequest.setReadTimeout(3 * 60000));

            for (String eventId : eventIdsChunk) {
                calendar.events().delete(calendarId, eventId).queue(batchRequest, new JsonBatchCallback<>() {
                    @Override
                    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
                        if (isAlreadyDeleted(e)) {
                            log.debug("Событие {} уже было удалено ранее", eventId);
                            return;
                        }

                        log.error("Не удалось удалить событие {}: {}", eventId, e.getMessage());
                        errors.add(e);
                    }

                    @Override
                    public void onSuccess(Void unused, HttpHeaders responseHeaders) {
                        log.trace("Удалено событие {}", eventId);
                    }
                });
            }

            batchRequest.execute();
        }

        if (!errors.isEmpty()) {
            throw new CalendarIntegrationException("Не удалось удалить событий: " + errors.size());
        }
    }

    /**
     * Удаление уже удаленного события не считаем ошибкой: это делает повторный вызов удаления безопасным
     */
    private boolean isAlreadyDeleted(GoogleJsonError error) {
        return error.getCode() == EVENT_ALREADY_DELETED_CODE || error.getCode() == EVENT_NOT_FOUND_CODE;
    }

    private void rollbackCreatedEvents(String calendarId, List<ResponseCreateEventDto> createdEvents) {
        List<String> createdEventsId = createdEvents.stream()
                .map(ResponseCreateEventDto::getEventId)
                .toList();

        try {
            deleteEvents(calendarId, createdEventsId);
            log.info("Удалены все созданные события из-за ошибок при попытке создать event");
        } catch (Exception e) {
            log.error("Не удалось удалить события после неудачного создания. Их придется удалить вручную: {}",
                    createdEventsId, e);
        }
    }

    /**
     * Google ограничивает батч 50 запросами, поэтому большие списки отправляем частями
     */
    private <T> List<List<T>> partition(List<T> source, int chunkSize) {
        List<List<T>> partitions = new ArrayList<>();

        for (int i = 0; i < source.size(); i += chunkSize) {
            partitions.add(source.subList(i, Math.min(i + chunkSize, source.size())));
        }

        return partitions;
    }

    private List<EventDto> getEventsBetweenDatesInternal(String calendarId, CalendarType calendarType, LocalDateTime leftDate, LocalDateTime rightDate, String eventName) {
        Date startTime = Date.from(leftDate.atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(rightDate.atZone(ZoneId.systemDefault()).toInstant());

        List<Events> events = getEventsBetweenDates(calendarId, eventName, startTime, endTime);
        Stream<EventDto> eventDtoStream = events.stream()
                .flatMap(e -> e.getItems().stream())
                .filter(this::isDefaultEvent)
                .map(event -> buildEvent(event, calendarType));

        if (eventName != null) {
            eventDtoStream = eventDtoStream.filter(eventDto -> eventDto.getSummary().equals(eventName));
        }

        List<EventDto> clientsList = eventDtoStream.toList();
        log.debug("Извлеченные встречи: {}", clientsList);
        return clientsList;
    }

    private boolean isDefaultEvent(Event event) {
        Object eventTypeObject = event.get("eventType");

        if (eventTypeObject == null) {
            return true; // скорее всего null невозможен и везде стоит default по умолчанию
        }

        if (eventTypeObject instanceof String eventType) {
            return DEFAULT_EVENT_TYPE.equalsIgnoreCase(eventType);
        }

        return false;
    }

    @SneakyThrows
    private List<Events> getEventsBetweenDates(String calendarId, String clientName, Date startTime, Date endTime) {
        int pageNumber = 1;

        List<Events> allEvents = new ArrayList<>();

        Events events = getEventsBetweenDates(calendarId, clientName, startTime, endTime, null);
        allEvents.add(events);
        log.debug("Для calendarId = [{}] [{} - {}] извлечена страница №[{}]", calendarId, startTime, endTime, pageNumber);

        while (events.getNextPageToken() != null) {
            events = getEventsBetweenDates(calendarId, clientName, startTime, endTime, events.getNextPageToken());
            allEvents.add(events);
            pageNumber++;
            log.debug("Для calendarId = [{}] [{} - {}] извлечена страница №[{}]", calendarId, startTime, endTime, pageNumber);
        }

        return allEvents;
    }

    private Events getEventsBetweenDates(String calendarId, String clientName, Date startTime, Date endTime, String nextPageToken) throws IOException {
        log.info("Send request to google");
        Events events = calendar.events().list(calendarId)
                .setTimeMin(new DateTime(startTime))
                .setTimeMax(new DateTime(endTime))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setPageToken(nextPageToken)
                .setQ(clientName)
                .execute();
        log.info("Response from google received");
        return events;
    }

    private EventDto buildEvent(com.google.api.services.calendar.model.Event event, CalendarType calendarType) {
        EventStatus eventStatus;
        if (calendarType == CalendarType.CANCELLED) {
            eventStatus = EventStatus.UNPLANNED_CANCELLED; // TODO если ивент пришел из отмененного календаря, то надо тоже высчитывать статус, они там не только незапланированные. поэтому сейчас учитываем этот календарь некорректно
        } else if (calendarType == CalendarType.PLAIN) {
            eventStatus = null;
        } else {
            PrimaryEventData primaryEventData = PrimaryEventData.builder()
                    .colorId(event.getColorId())
                    .name(event.getSummary())
                    .eventStart(event.getStart().getDateTime())
                    .build();

            eventStatus = eventStatusService.getEventStatus(primaryEventData);
        }

        try {
            return EventDto.builder()
                    .status(eventStatus)
                    .summary(event.getSummary())
                    .start(getLocalDateTime(event.getStart()))
                    .end(getLocalDateTime(event.getEnd()))
                    .eventExtensionDto(buildEventExtension(event, calendarType))
                    .build();
        } catch (Exception e) {
            throw new CannotBuildEventIntegrationException(event.getSummary(), event.getStart(), e); //TODO тут надо как-то разбить исключния которе вылетают мои от тех, которые выкидываются сами по себе
        }
    }

    private LocalDateTime getLocalDateTime(EventDateTime eventDateTime) {
        DateTime date;
        if (eventDateTime.getDateTime() != null) {
            date = eventDateTime.getDateTime();  // событие со временем и датой
        } else {
            date = eventDateTime.getDate(); // событие с датой на весь день
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getValue()), ZoneId.systemDefault());
    }

    @Nullable
    private EventExtensionDto buildEventExtension(com.google.api.services.calendar.model.Event event, CalendarType calendarType) {
        if (calendarType == CalendarType.PLAIN || event.getDescription() == null) {
            return null;
        }

        EventExtensionDto eventExtensionDtoFromPlainText = buildFromPlainText(event);
        if (eventExtensionDtoFromPlainText != null) {
            return eventExtensionDtoFromPlainText;
        }
        log.trace("Не удалось собрать EventExtensionDto в виде PLAIN TEXT, неверный формат, event = {}", event);

        EventExtensionDto extensionDtoFromJson = buildFromJson(event);
        if (extensionDtoFromJson != null) {
            return extensionDtoFromJson;
        }

        log.error("Не удалось собрать EventExtensionDto ни одним из вариантов, неверный формат расширения события. {}", event);
        throw new CannotBuildEventIntegrationException(event.getSummary(), event.getStart());
    }

    private EventExtensionDto buildFromJson(Event event) {
        try {
            return objectMapper.readValue(event.getDescription(), EventExtensionDto.class);
        } catch (Exception e) {
            return null;
        }
    }

    private EventExtensionDto buildFromPlainText(Event event) {
        try {
            return PlainTextMapper.fillFromString(event.getDescription(), EventExtensionDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}
