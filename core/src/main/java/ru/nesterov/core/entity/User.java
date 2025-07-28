package ru.nesterov.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@ToString(exclude = "clients")
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
    private boolean isEventsBackupEnabled;

    @OneToMany(mappedBy = "user")
    private List<Client> clients;
}
