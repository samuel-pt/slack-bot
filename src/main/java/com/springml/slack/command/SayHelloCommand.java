package com.springml.slack.command;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.springml.slack.command.model.Command;
import com.springml.slack.resource.SlackResource;

public class SayHelloCommand implements CommandExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(SayHelloCommand.class);
    @Autowired
    private SlackResource slackResource;

    @PostConstruct
    public void init() {
        slackResource.registerExecutor("/say_hello", this);
    }

    @Override
    public String execute(Command command) throws Exception {
        LOG.info("Command to be executed " + command);
        return "Hi " + command.getText();
    }

}
