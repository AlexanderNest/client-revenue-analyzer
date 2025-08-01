package ru.nesterov.bot.handlers.abstractions;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.core.entity.Role;

import java.util.List;

/**
 * Обработчик, который вызывается по отправленной команде
 */


public abstract class InvocableCommandHandler extends SendingMessageCommandHandler {
    /**
     * Команда, которая вызовет обработчик
     */

    @Autowired
    protected ClientRevenueAnalyzerIntegrationClient client;

    public abstract String getCommand();

    private boolean isApplicableRole(Role role) {
        return getApplicableRoles().contains(role);
    }

    protected List<Role> getApplicableRoles() {
        return List.of(Role.USER);
    }

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();

        boolean isCurrentHandlerCommand = message != null && getCommand().equals(message.getText());
        if (isCurrentHandlerCommand) {
            return true;
        }

//        GetUserRequest getUserRequest = new GetUserRequest();
//        getUserRequest.setUsername(String.valueOf(TelegramUpdateUtils.getUserId(update)));
//        GetUserResponse response = client.getUserByUsername(getUserRequest);
//        if (!(isApplicableRole(response.getRole()))) {
//            return false;
//        }

        CallbackQuery callbackQuery = update.getCallbackQuery();

        if (callbackQuery == null) {
            return false;
        }

        boolean isShortButtonCallback = getCommand().equals(buttonCallbackService.buildButtonCallback(callbackQuery.getData()).getCommand());
        boolean isJsonButtonCallback = false;
        try {
            isJsonButtonCallback = getCommand().equals(objectMapper.readValue(callbackQuery.getData(), ButtonCallback.class).getCommand());
        } catch (Exception ignored) {

        }

        return isShortButtonCallback || isJsonButtonCallback;
    }

}
