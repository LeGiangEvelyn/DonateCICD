package com.leap.donate.service.impl;

import com.leap.donate.model.CurrentScore;
import com.leap.donate.model.RemainingPoints;
import com.leap.donate.model.Transaction;
import com.leap.donate.model.User;
import com.leap.donate.service.DonateService;
import com.leap.donate.service.ScoreService;
import com.leap.donate.service.SlackService;
import com.leap.donate.service.TransactionService;
import com.leap.donate.service.UserService;
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.slack.api.methods.SlackApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DonateServiceImpl implements DonateService {

    private final UserService userService;
    private final ScoreService scoreService;
    private final TransactionService transactionService;
    private final SlackService slackService;
    private final Random random = new Random();

    @Value("${app.max-points-per-month}")
    private Integer maxPointsPerMonth;

    @Value("${slack.donate-channel-name}")
    private String donateChannelName;

    // Regex pattern to match the donation command format: /i-want-to-give @username {score} message
    private static final Pattern DONATION_PATTERN = Pattern.compile(
            "(?i)\\s*@([\\w.-]+)\\s+(\\d+)\\s+(.+)\\s*");

    @Override
    @Transactional
    public SlashCommandResponse givePoints(String userId, String text, String channelId) throws IOException, SlackApiException {
        log.debug("Processing give points command from user {}: {}", userId, text);
        
        try {
            // Parse command and validate input
            DonationCommand donationCommand = parseDonationCommand(text);
            if (donationCommand == null) {
                return slackService.buildErrorResponse("Invalid format. Use: `/i-want-to-give @username {score} message`");
            }

            // Get and validate users
            User sender = getValidatedSender(userId);
            User recipient = getValidatedRecipient(donationCommand.getRecipientUsername());
            
            // Validate transaction
            validateTransaction(sender, recipient, donationCommand.getPoints());
            
            // Process the donation
            processDonation(sender, recipient, donationCommand);
            
            // Return success response
            return buildSuccessResponse(sender, recipient, donationCommand);
            
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return slackService.buildErrorResponse("Security violation: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error giving points", e);
            return slackService.buildErrorResponse("Error: " + e.getMessage());
        }
    }

    private static class DonationCommand {
        private final String recipientUsername;
        private final int points;
        private final String message;

        public DonationCommand(String recipientUsername, int points, String message) {
            this.recipientUsername = recipientUsername;
            this.points = points;
            this.message = message;
        }

        public String getRecipientUsername() { return recipientUsername; }
        public int getPoints() { return points; }
        public String getMessage() { return message; }
    }

    private DonationCommand parseDonationCommand(String text) {
        Matcher matcher = DONATION_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return null;
        }

        String recipientUsername = matcher.group(1);
        int points;
        try {
            points = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid coins value. Must be a number.");
        }

        if (points <= 0) {
            throw new RuntimeException("Coins must be greater than 0.");
        }

        String message = matcher.group(3);
        return new DonationCommand(recipientUsername, points, message);
    }

    private User getValidatedSender(String userId) throws IOException, SlackApiException {
        com.slack.api.model.User slackSender = slackService.getSlackUserById(userId);
        try {
            return userService.getOrCreateUserFromSlack(
                    slackSender.getId(),
                    slackSender.getName(),
                    slackSender.getRealName(),
                    slackSender.getId()
            );
        } catch (RuntimeException e) {
            log.error("Error getting validated sender", e);
            throw new RuntimeException("You are not registered in the workspace. Please contact an administrator.");
        }
    }

    private User getValidatedRecipient(String recipientUsername) throws IOException, SlackApiException {
        com.slack.api.model.User slackRecipient = slackService.getSlackUserByUsername(recipientUsername);
        try {
            return userService.getOrCreateUserFromSlack(
                    slackRecipient.getId(),
                    slackRecipient.getName(),
                    slackRecipient.getRealName(),
                    slackRecipient.getId()
            );
        } catch (RuntimeException e) {
            log.error("Error getting validated recipient", e);
            throw new RuntimeException("Recipient user @" + recipientUsername + " is not registered in the workspace.");
        }
    }

    private void validateTransaction(User sender, User recipient, int points) {
        // Validate sender and recipient
        if (sender == null || recipient == null) {
            throw new SecurityException("Invalid sender or recipient");
        }

        // Check if users are active
        if (sender.getStatus() == User.UserStatus.DEACTIVATED) {
            throw new SecurityException("Your account is deactivated. Please contact an administrator.");
        }
        if (recipient.getStatus() == User.UserStatus.DEACTIVATED) {
            throw new SecurityException("Cannot give points to a deactivated user.");
        }

        // Prevent self-donation
        if (sender.getId().equals(recipient.getId())) {
            throw new SecurityException("Cannot give points to yourself");
        }

        // Validate points amount
        if (points <= 0 || points > maxPointsPerMonth) {
            throw new SecurityException("Invalid points amount");
        }

        // Check for suspicious patterns
        if (transactionService.hasRecentTransactions(sender.getId(), 10)) {
            throw new SecurityException("Too many transactions in a short period. Please wait a few minutes.");
        }
        
        // Validate remaining points
        RemainingPoints remainingPoints = scoreService.getRemainingPoints(sender);
        if (remainingPoints.getRemainingPoints() < points) {
            throw new SecurityException("Not enough points to give. You have " + remainingPoints.getRemainingPoints() + " points remaining.");
        }
    }

    private void processDonation(User sender, User recipient, DonationCommand command) throws IOException, SlackApiException {
        // Deduct points from sender
        scoreService.deductPoints(sender, command.getPoints());
        
        // Add points to recipient
        scoreService.addPoints(recipient, command.getPoints());
        
        // Create transaction record
        transactionService.createTransaction(sender, recipient, command.getPoints(), command.getMessage());
        
        // Post message to donate channel
        String donateChannelId = slackService.createOrGetDonateChannel();
        String announcementMessage = String.format(
                "@here <@%s> gives %d to <@%s>: \"%s\"",
                sender.getSlackId(), command.getPoints(), recipient.getSlackId(), command.getMessage()
        );
        slackService.postMessage(donateChannelId, announcementMessage);
    }

    private SlashCommandResponse buildSuccessResponse(User sender, User recipient, DonationCommand command) {
        RemainingPoints remainingPoints = scoreService.getRemainingPoints(sender);
        return slackService.buildSlashCommandResponse(
                String.format("Successfully gave %d coins to <@%s>. You have %d coins remaining.",
                        command.getPoints(), recipient.getSlackId(), remainingPoints.getRemainingPoints()));
    }

    @Override
    @Transactional
    public SlashCommandResponse showUserInfo(String userId, String channelId) throws IOException, SlackApiException {
        log.debug("Processing me command from user {}", userId);
        
        try {
            User user = getUserFromSlack(userId);
            
            // Get user's current score and remaining points
            CurrentScore currentScore = scoreService.getCurrentScore(user);
            RemainingPoints remainingPoints = scoreService.getRemainingPoints(user);
            
            // Build response
            StringBuilder response = new StringBuilder();
            response.append("*Your Detail Coins*\n");
            response.append("Current Coins: ").append(currentScore.getScore()).append(" coins\n");
            response.append("Remaining Coins to Give: ").append(remainingPoints.getRemainingPoints()).append("/").append(maxPointsPerMonth).append("\n");
            
            return slackService.buildSlashCommandResponse(response.toString());
            
        } catch (RuntimeException e) {
            log.error("Error showing user info", e);
            return slackService.buildErrorResponse("Error: " + e.getMessage());
        }
    }

    private User getUserFromSlack(String userId) throws IOException, SlackApiException {
        com.slack.api.model.User slackUser = slackService.getSlackUserById(userId);
        try {
            return userService.getOrCreateUserFromSlack(
                    slackUser.getId(),
                    slackUser.getName(),
                    slackUser.getRealName(),
                    slackUser.getId()
            );
        } catch (RuntimeException e) {
            log.error("Error getting user from Slack", e);
            throw new RuntimeException("You are not registered in the workspace. Please contact an administrator.");
        }
    }

    @Override
    public SlashCommandResponse showHelp() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("*Available Commands:*\n\n");
        helpText.append("*/i-want-to-give @username <amount>* - Give coins to someone\n");
        helpText.append("*/mine* - Show your current coin balance\n");
        helpText.append("*/top-ten* - Show top 10 users with highest coins this month\n");
        helpText.append("*/help* - Show this help message\n");
        return slackService.buildSlashCommandResponse(helpText.toString());
    }

    @Override
    public SlashCommandResponse showTopTen(String channelId) throws IOException, SlackApiException {
        log.info("Showing top 10 users for current month");
        
        // Get current month and year
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        
        // Get top 10 users for current month (only active users)
        List<CurrentScore> topScores = scoreService.getTop10Scores(currentMonth, currentYear)
            .stream()
            .filter(score -> score.getUser().getStatus() == User.UserStatus.ACTIVE)
            .collect(Collectors.toList());
        
        if (topScores.isEmpty()) {
            return slackService.buildSlashCommandResponse("No coins recorded for this month yet.");
        }
        
        // Build response message
        StringBuilder message = new StringBuilder("*Top 10 Users for " + now.getMonth().toString() + " " + currentYear + "*\n\n");
        
        for (int i = 0; i < topScores.size(); i++) {
            CurrentScore score = topScores.get(i);
            String displayName = score.getUser().getRealName() != null ? score.getUser().getRealName() : score.getUser().getUsername();
            
            message.append(String.format("%d. *%s* - %d coins%n", 
                i + 1, 
                displayName,
                score.getScore()));
        }
        
        return slackService.buildSlashCommandResponse(message.toString());
    }
} 