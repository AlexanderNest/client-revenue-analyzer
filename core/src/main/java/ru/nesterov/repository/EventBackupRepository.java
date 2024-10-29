package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.EventBackup;

import java.time.LocalDateTime;

@Repository
public interface EventBackupRepository extends JpaRepository<EventBackup, Long> {
    boolean existsByUserIdAndManualBackupTimeAfter(long userId, LocalDateTime checkedTime);
    
    boolean existsByAutomaticBackupTimeAfter(LocalDateTime checkedTime);
}
