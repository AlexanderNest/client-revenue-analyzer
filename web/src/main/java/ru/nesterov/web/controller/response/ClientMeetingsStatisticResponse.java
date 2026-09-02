package ru.nesterov.web.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ClientMeetingsStatisticResponse {
    private String name;
    private long id;
    private String description;
    private Date startDate;
    private long serviceDuration;
    private String phone;
    private double successfulMeetingsHours;
    private double cancelledMeetingsHours;
    private double promoMeetingsHours;
    private double incomePerHour;
    private int successfulEventsCount;
    private int plannedCancelledEventsCount;
    private int notPlannedCancelledEventsCount;
    private int promoEventsCount;
    private int plannedEventsCount;
    private int requiresShiftEventsCount;
    private double totalIncome;
}
