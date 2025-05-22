package com.leap.donate.config;

import com.leap.donate.service.SlackService;
import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.TeamJoinEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SlackConfig {

    @Value("${slack.bot-token}")
    private String botToken;

    @Value("${slack.signing-secret}")
    private String signingSecret;
    
    private final SlackService slackService;

    @Bean
    public App slackApp() {
        AppConfig appConfig = AppConfig.builder()
                .singleTeamBotToken(botToken)
                .signingSecret(signingSecret)
                .build();
        
        App app = new App(appConfig);
        
        // Register the team_join event handler
        app.event(TeamJoinEvent.class, (payload, ctx) -> {
            try {
                // Get the new user
                com.slack.api.model.User newUser = payload.getEvent().getUser();
                log.info("New user joined: {}", newUser.getName());
                
                // Get the donate channel ID
                String donateChannelId = slackService.createOrGetDonateChannel();
                
                // Invite the new user to the channel
                ctx.client().conversationsInvite(r -> r
                        .channel(donateChannelId)
                        .users(List.of(newUser.getId())));
                
                log.info("Added new user {} to donate channel", newUser.getName());
                
                return ctx.ack();
            } catch (Exception e) {
                log.error("Error adding new user to donate channel", e);
                return ctx.ack();
            }
        });
        
        return app;
    }
} 