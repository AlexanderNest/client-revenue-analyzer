package ru.nesterov.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(name = "main_calendar_id", nullable = false)
    private String mainCalendar;
    @Column(name = "cancelled_calendar_id")
    private String cancelledCalendar;
    @Column(name = "cancelled_calendar_enabled")
    private boolean isCancelledCalendarEnabled;
    @Column(name = "events_backup_enabled")
    private boolean isEventsBackupEnabled = true;

    @OneToMany(mappedBy = "user")
    private List<Client> clients;
}
