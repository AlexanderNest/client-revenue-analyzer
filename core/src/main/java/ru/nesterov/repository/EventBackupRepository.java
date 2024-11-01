package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.EventBackup;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventBackupRepository extends JpaRepository<EventBackup, Long> {
    long countAllByUserIdIn(List<Long> userIds);
    
    boolean existsByUserIdAndBackupTimeAfter(long userId, LocalDateTime checkedTime);
    
    boolean existsByUserIdInAndBackupTimeBefore(List<Long> userIds, LocalDateTime checkedTime);
}
