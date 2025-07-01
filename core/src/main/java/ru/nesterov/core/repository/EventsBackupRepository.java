package ru.nesterov.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nesterov.core.entity.BackupType;
import ru.nesterov.core.entity.EventBackup;

import java.time.LocalDateTime;

@Repository
public interface EventsBackupRepository extends JpaRepository<EventBackup, Long> {
    EventBackup findByTypeAndUserIdAndBackupTimeAfter(BackupType type, long userId, LocalDateTime checkedTime);
    
    boolean existsByTypeAndBackupTimeAfter(BackupType type, LocalDateTime checkedTime);
}
