package com.springml.slack.command;

import com.springml.slack.command.model.Command;

public interface CommandExecutor {
    public String execute(Command command) throws Exception;
}
