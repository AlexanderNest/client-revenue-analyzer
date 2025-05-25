package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    
    List<User> findAllByIsEventsBackupEnabled(boolean isEventsBackupEnabled);
}
