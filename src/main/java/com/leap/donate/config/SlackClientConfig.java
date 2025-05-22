package com.leap.donate.config;

import com.slack.api.Slack;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackClientConfig {
    
    @Bean
    public Slack slackClient() {
        return Slack.getInstance();
    }
} 