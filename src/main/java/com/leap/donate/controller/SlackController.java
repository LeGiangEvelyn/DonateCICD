package com.leap.donate.controller;

import com.leap.donate.service.DonateService;
import com.leap.donate.service.SlackService;
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.slack.api.methods.SlackApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/slack")
@RequiredArgsConstructor
@Slf4j
public class SlackController {

    private final DonateService donateService;
    private final SlackService slackService;

    @PostMapping(value = "/commands", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<SlashCommandResponse> handleSlashCommand(
            @RequestParam("command") String command,
            @RequestParam("user_id") String userId,
            @RequestParam("channel_id") String channelId,
            @RequestParam(value = "text", required = false) String text) throws IOException, SlackApiException {

        // Validate required parameters
        if (command == null || userId == null || channelId == null) {
            log.warn("Missing required parameters");
            return ResponseEntity.badRequest().body(
                slackService.buildErrorResponse("Missing required parameters")
            );
        }

        // Validate command format
        if (!command.startsWith("/")) {
            log.warn("Invalid command format: {}", command);
            return ResponseEntity.badRequest().body(
                slackService.buildErrorResponse("Invalid command format")
            );
        }

        log.info("Processing slash command: {} from user: {}", command, userId);
        
        // Process recognized commands
        SlashCommandResponse response;
        switch (command) {
            case "/i-want-to-give":
                response = donateService.givePoints(userId, text, channelId);
                break;
            case "/mine":
                response = donateService.showUserInfo(userId, channelId);
                break;
            case "/help":
                response = donateService.showHelp();
                break;
            case "/top-ten":
                response = donateService.showTopTen(channelId);
                break;
            default:
                log.warn("Unknown command received: {}", command);
                response = slackService.buildErrorResponse("Unknown command. Use /help to see available commands.");
                break;
        }
        return ResponseEntity.ok(response);
    }
} 