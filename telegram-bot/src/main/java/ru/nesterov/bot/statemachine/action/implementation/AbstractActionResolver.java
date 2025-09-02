package ru.nesterov.bot.statemachine.action.implementation;

import ru.nesterov.bot.statemachine.action.ActionResolver;

public abstract class AbstractActionResolver implements ActionResolver {
    @Override
    public String toString() {
        return getClass().getSimpleName() + "; Order = " + getOrder();
    }
}
