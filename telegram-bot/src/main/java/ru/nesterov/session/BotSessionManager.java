package ru.nesterov.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.nesterov.bot.handlers.CommandHandler;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BotSessionManager {
//    private final Map<Class<CommandHandler>, Map<Long, Object>> session;
//
//    public <T> T getData(Class<CommandHandler> commandHandlerClass, Long id, Class<T> resultClass) {
//        return (T) session.get(commandHandlerClass).get(id);
//    }
    private final Map<Long, UserData> sessions = new ConcurrentHashMap<>();

    public UserData getUserData(long chatId, long userId) {
        return sessions.computeIfAbsent(chatId, key -> UserData.builder()
                .userId(userId)
                .build());
    }

    public void setDefaultUserData(UserData userData) {
        userData.setClientName(null);
        userData.setCurrentDate(LocalDate.now());
        userData.setFirstDate(null);
        userData.setSecondDate(null);
    }
}
