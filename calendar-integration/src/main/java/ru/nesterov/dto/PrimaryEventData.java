package ru.nesterov.dto;

import com.google.api.client.util.DateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrimaryEventData {
    private String name;
    private String colorId;
    private DateTime eventStart;
}
