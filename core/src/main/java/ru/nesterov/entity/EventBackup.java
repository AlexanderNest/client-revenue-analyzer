package ru.nesterov.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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
    
    @CreationTimestamp
    private LocalDateTime backupTime;
    
    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "event_backup_id")
    private List<Event> events;
}
