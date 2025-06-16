package ru.nesterov.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class FullClientInfoResponse {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private Date startDate;
    private long serviceDuration;
    private String phone;
    private int totalMeetings;
    private double totalMeetingsHours;
    private double totalIncome;
    private int unplannedCancelledEventsCount;
    private int plannedCancelledEventsCount;
}
