package ru.nesterov.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import ru.nesterov.common.dto.EventStatus;
import ru.nesterov.entity.converter.EventExtensionAttributeConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
@Data
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private EventStatus status;
    private String summary;
    
    @Column(name = "start_date")
    private LocalDateTime start;
    
    @Column(name = "end_date")
    private LocalDateTime end;
    
    @Convert(converter = EventExtensionAttributeConverter.class)
    private EventExtension eventExtension;
}

