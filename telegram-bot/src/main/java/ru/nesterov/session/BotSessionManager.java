package ru.nesterov.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static ru.nesterov.session.SessionState.AWAITING_FIRST_DATE;
import static ru.nesterov.session.SessionState.AWAITING_NAME;
import static ru.nesterov.session.SessionState.AWAITING_SECOND_DATE;
import static ru.nesterov.session.SessionState.COMPLETED;

@Component
@RequiredArgsConstructor
public class BotSessionManager {
    private final Map<Long, UserSession> sessions = new HashMap<>();

    public UserSession getSession(long chatId) {
        return sessions.computeIfAbsent(chatId, id ->
                UserSession.builder()
                        .chatId(id)
                        .state(AWAITING_NAME)
                        .build());
    }

    public void toDefaultUserSession(UserSession userSession) {
        userSession.setClientName(null);
        userSession.setFirstDate(null);
        userSession.setSecondDate(null);
        userSession.setState(AWAITING_NAME);
    }

    public void changeState(UserSession session) {
        switch (session.getState()) {
            case AWAITING_NAME:
                session.setState(AWAITING_FIRST_DATE);
                break;
            case AWAITING_FIRST_DATE:
                session.setState(AWAITING_SECOND_DATE);
                break;
            case AWAITING_SECOND_DATE:
                session.setState(COMPLETED);
                break;
        }
    }
}
