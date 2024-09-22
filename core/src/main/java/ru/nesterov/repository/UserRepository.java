package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nesterov.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
