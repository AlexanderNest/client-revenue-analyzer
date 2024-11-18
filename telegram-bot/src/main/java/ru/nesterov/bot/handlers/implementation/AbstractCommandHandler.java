package ru.nesterov.bot.handlers.implementation;

import ru.nesterov.bot.handlers.CommandHandler;

/**
 * Любой CommandHandler
 */
public abstract class AbstractCommandHandler implements CommandHandler {
    public abstract String getCommand();
}
