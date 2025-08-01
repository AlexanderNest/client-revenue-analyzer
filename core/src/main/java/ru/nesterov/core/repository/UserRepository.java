package ru.nesterov.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nesterov.core.entity.Role;
import ru.nesterov.core.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    List<User> findAllByIsEventsBackupEnabled(boolean isEventsBackupEnabled);

    @Query("SELECT u.username FROM User u WHERE u.role = :role AND u.source = :source")
    List<String> findUsersIdByRoleAndSource(Role role, String source);
}
