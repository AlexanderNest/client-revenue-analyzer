package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("SELECT u FROM User u JOIN u.userSettings us WHERE us.isEventsBackupEnabled = :isEventsBackupEnabled")
    List<User> findAllByIsEventsBackupEnabled(@Param("isEventsBackupEnabled") boolean isEventsBackupEnabled);
}
