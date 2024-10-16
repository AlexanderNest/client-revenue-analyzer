package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.EventBackup;

import java.util.List;

@Repository
public interface EventBackupRepository extends JpaRepository<EventBackup, Long> {
    @Modifying
    List<EventBackup> removeAllByUserId(long userId);
    
    EventBackup findFirstByUserId(long userId);
}
