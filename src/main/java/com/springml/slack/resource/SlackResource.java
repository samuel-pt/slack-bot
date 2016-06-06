package com.springml.slack.resource;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.springml.slack.command.CommandExecutor;
import com.springml.slack.command.model.Command;

@RestController
public class SlackResource {
    private static final Logger LOG = LoggerFactory.getLogger(SlackResource.class);

    private Map<String, CommandExecutor> commands = new HashMap<>();

    public void registerExecutor(String command, CommandExecutor executor) {
        LOG.info("Registering " + command);
        commands.put(command, executor);
    }

    @RequestMapping(value = "/slack/oauth", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<String> oauth(@RequestParam("code") String code,
            @RequestParam(name = "state", required = false) String state) throws Exception {

        System.out.println("code : " + code);
        System.out.println("state : " + state);
        return new ResponseEntity<String>("message", HttpStatus.OK);
    }

    @RequestMapping(value = "/slack/{slackCommand}", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> command(@ModelAttribute Command command) throws Exception {
        LOG.info("command : " + command);
        LOG.info("Commands : " + commands);
        String resTxt = commands.get(command.getCommand()).execute(command);
        LOG.info("Response Text : " + resTxt);
        return new ResponseEntity<String>(resTxt, HttpStatus.OK);
    }

}
