package com.springml.slack.command;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SayHelloCommand {

    @RequestMapping(value = "/slack/oauth", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<String> oauth(@RequestParam("code") String code,
            @RequestParam(name = "state", required = false) String state) throws Exception {

        System.out.println("code : " + code);
        System.out.println("state : " + state);
        return new ResponseEntity<String>("message", HttpStatus.OK);
    }

}
