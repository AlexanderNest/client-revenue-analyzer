package ru.nesterov.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nesterov.core.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    
    List<User> findAllByIsEventsBackupEnabled(boolean isEventsBackupEnabled);
}
