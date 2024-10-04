package ru.nesterov.bot.handlers.callback;

import lombok.Data;

@Data
public class ButtonCallback {
    private String command;
    private String value;

    public String toShortString() {
        return command + ":" + value;
    }

    public static ButtonCallback fromShortString(String callbackData) {
        String[] parts = callbackData.split(":");
        ButtonCallback buttonCallback = new ButtonCallback();
        buttonCallback.setCommand(parts[0]);
        buttonCallback.setValue(parts[1]);

        return buttonCallback;
    }
}
