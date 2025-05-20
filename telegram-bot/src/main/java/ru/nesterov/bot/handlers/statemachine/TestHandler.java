package ru.nesterov.bot.handlers.statemachine;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.statemachine.StateMachine;
import ru.nesterov.statemachine.dto.Action;
import ru.nesterov.statemachine.dto.NextStateFunction;

@ConditionalOnProperty("bot.enabled")
@Component
public class TestHandler extends DisplayedCommandHandler {
    private final StateMachine<State, Action, TestDto> stateMachine = new StateMachine<>(State.STARTED, new TestDto());

    public TestHandler() {
        stateMachine.addTransition(State.STARTED, Action.COMMAND_INPUT, State.WAITING_1, update -> getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Ожидаю Ввод 1"));
        stateMachine.addTransition(State.WAITING_1, Action.ANY_STRING, State.WAITING_2, update -> {
            String text = update.getMessage().getText();
            stateMachine.getMemory().fist = text;
            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Помню: " + stateMachine.getMemory() + " Ожидаю Ввод 2");
        });
        stateMachine.addTransition(State.WAITING_2, Action.ANY_STRING, State.WAITING_3, update -> {
            String text = update.getMessage().getText();
            stateMachine.getMemory().second = text;
            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Помню: " + stateMachine.getMemory() + " Ожидаю Ввод 3");
        });
        stateMachine.addTransition(State.WAITING_3, Action.ANY_STRING, State.FINISHED, update -> {
            String text = update.getMessage().getText();
            stateMachine.getMemory().third = text;
            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Помню: " + stateMachine.getMemory() + " Завершен");
        });
    }

    @Override
    public String getCommand() {
        return "/test";
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        String text = update.getMessage().getText();
        Action action;

        if (text.equals(getCommand())) {
            action = Action.COMMAND_INPUT;
        } else {
            action = Action.ANY_STRING;
        }

        NextStateFunction<State, BotApiMethod<?>, Update> nextStateFunction = stateMachine.getNextStateFunction(action);
        BotApiMethod<?> botApiMethod = nextStateFunction.getFunctionForTransition().apply(update);
        stateMachine.applyNextState(action);

        return botApiMethod;
    }

    @Override
    public boolean isFinished(Long userId) {
        return stateMachine.getCurrentState() == State.FINISHED;
    }
}
