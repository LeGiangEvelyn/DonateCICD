package com.leap.donate;

import com.leap.donate.service.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class DonateApplication {

    private final SlackService slackService;
    private final Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(DonateApplication.class, args);
    }

    /**
     * Initializes the application by creating the #donate channel and adding all users to it.
     * Skips initialization when running with local profile.
     */
    @Bean
    public CommandLineRunner initializeApplication() {
        return args -> {
            try {
                log.info("Initializing application...");
                
                // Create or get the #donate channel and add all users
                String donateChannelId = slackService.createOrGetDonateChannel();
                slackService.addUsersToDonateChannel(donateChannelId);
                
                log.info("Application initialization completed successfully");
            } catch (Exception e) {
                log.error("Error during application initialization", e);
            }
        };
    }

} 