package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.User;
import ru.nesterov.entity.UserSetting;

import java.util.List;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSetting, Long> {
    List<User> findAllBy(boolean isEventsBackupEnabled);
}
