package com.springml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;

import com.springml.slack.command.OpportunitiesCommand;
import com.springml.slack.command.SayHelloCommand;
import com.springml.slack.command.cache.OpportunityChangesCache;
import com.springml.slack.command.cache.OpportunityListCache;
import com.springml.slack.resource.SlackResource;

@SpringBootApplication
public class SlackBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlackBotApplication.class, args);
    }

    @Bean
    public SlackResource slackResource() {
        return new SlackResource();
    }

    @Bean
    public CacheManager cacheManager() {
        return new GuavaCacheManager(OpportunityChangesCache.class.getSimpleName(),
                OpportunityListCache.class.getSimpleName());
    }

    @Bean
    public OpportunityChangesCache opportunityChangesCache() {
        return new OpportunityChangesCache();
    }

    @Bean
    public OpportunityListCache opportunityListCache() {
        return new OpportunityListCache();
    }

    @Bean
    public SayHelloCommand sayHelloCommand() {
        return new SayHelloCommand();
    }

    @Bean
    public OpportunitiesCommand opportunitiesCommand() {
        return new OpportunitiesCommand();
    }

}
