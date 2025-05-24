package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("SELECT id FROM User")
    List<Long> findAllUserIds();
    
    List<User> findAllByIsEventsBackupEnabled(boolean isEventsBackupEnabled);
}
