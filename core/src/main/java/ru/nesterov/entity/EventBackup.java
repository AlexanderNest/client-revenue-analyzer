package ru.nesterov.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import ru.nesterov.dto.EventStatus;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "event_backup")
public class EventBackup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private EventStatus status;
    private String summary;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String comment;
    private Integer income;
    
    @CreationTimestamp
    private LocalDateTime backupTime;
}
