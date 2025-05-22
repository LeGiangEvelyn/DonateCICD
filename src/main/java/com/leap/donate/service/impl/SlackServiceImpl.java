package com.leap.donate.service.impl;

import com.leap.donate.model.User;
import com.leap.donate.service.SlackService;
import com.leap.donate.service.UserService;
import com.slack.api.Slack;
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsCreateRequest;
import com.slack.api.methods.request.conversations.ConversationsInviteRequest;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.methods.request.users.UsersListRequest;
import com.slack.api.methods.response.conversations.ConversationsCreateResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.ConversationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackServiceImpl implements SlackService {

    private final Slack slack;
    private final UserService userService;
    private final Environment environment;

    @Value("${slack.bot-token}")
    private String botToken;

    @Value("${slack.donate-channel-name}")
    private String donateChannelName;
    
    // Add cache for users
    private List<com.slack.api.model.User> cachedUsers = null;
    private long lastUserFetchTime = 0;
    private static final long USER_CACHE_DURATION = 3600000; // 1 hour in milliseconds

    @Override
    public void postMessage(String channelId, String message) throws IOException, SlackApiException {
        log.debug("Posting message to channel {}: {}", channelId, message);
        MethodsClient methods = slack.methods(botToken);
        methods.chatPostMessage(ChatPostMessageRequest.builder()
                .channel(channelId)
                .text(message)
                .mrkdwn(true)
                .build());
    }

    @Override
    public void postEphemeralMessage(String channelId, String userId, String message) throws IOException, SlackApiException {
        log.debug("Posting ephemeral message to user {} in channel {}: {}", userId, channelId, message);
        MethodsClient methods = slack.methods(botToken);
        methods.chatPostEphemeral(ChatPostEphemeralRequest.builder()
                .channel(channelId)
                .user(userId)
                .text(message)
                .build());
    }

    @Override
    public String createOrGetDonateChannel() throws IOException, SlackApiException {
        log.debug("Creating or getting donate channel");
        MethodsClient methods = slack.methods(botToken);
        
        // Check if channel already exists
        ConversationsListRequest listRequest = ConversationsListRequest.builder()
                .types(Collections.singletonList(ConversationType.PUBLIC_CHANNEL))
                .build();
        ConversationsListResponse listResponse = methods.conversationsList(listRequest);
        
        // Handle null response
        List<Conversation> channels = listResponse.getChannels();
        if (channels == null) {
            log.warn("Received null channels list from Slack API");
            channels = Collections.emptyList();
        }
        
        Optional<Conversation> existingChannel = channels.stream()
                .filter(channel -> channel.getName().equals(donateChannelName))
                .findFirst();
        
        if (existingChannel.isPresent()) {
            log.debug("Donate channel already exists with ID: {}", existingChannel.get().getId());
            return existingChannel.get().getId();
        }
        
        // Create the channel if it doesn't exist
        log.info("Creating donate channel: {}", donateChannelName);
        ConversationsCreateResponse createResponse = methods.conversationsCreate(ConversationsCreateRequest.builder()
                .name(donateChannelName)
                .build());
        
        if (!createResponse.isOk()) {
            log.error("Failed to create donate channel: {}", createResponse.getError());
            throw new RuntimeException("Failed to create donate channel: " + createResponse.getError());
        }
        
        log.info("Created donate channel with ID: {}", createResponse.getChannel().getId());
        return createResponse.getChannel().getId();
    }

    @Override
    public void addUsersToDonateChannel(String channelId) throws IOException, SlackApiException {
        log.debug("Adding users to channel: {}", channelId);
        
        MethodsClient methods = slack.methods(botToken);
        
        // Get all users from Slack
        List<com.slack.api.model.User> slackUsers = methods.usersList(UsersListRequest.builder().build())
            .getMembers();
        
        // Get all users from our database
        List<User> dbUsers = userService.getAllUsers();
        Map<String, User.UserStatus> userStatusMap = dbUsers.stream()
            .collect(Collectors.toMap(User::getId, User::getStatus));
        
        // Get current channel members
        var channelMembers = methods.conversationsMembers(r -> r.channel(channelId)).getMembers();
        
        // Invite each user to the channel
        for (com.slack.api.model.User slackUser : slackUsers) {
            if (!slackUser.isBot() && !slackUser.isDeleted() && 
                userStatusMap.getOrDefault(slackUser.getId(), User.UserStatus.DEACTIVATED) == User.UserStatus.ACTIVE &&
                !channelMembers.contains(slackUser.getId())) {
                try {
                    methods.conversationsInvite(ConversationsInviteRequest.builder()
                            .channel(channelId)
                            .users(Collections.singletonList(slackUser.getId()))
                            .build());
                    log.debug("Added user {} to channel", slackUser.getName());
                } catch (SlackApiException e) {
                    log.warn("Could not add user {} to channel: {}", slackUser.getName(), e.getMessage());
                }
            }
        }
    }

    @Override
    public List<com.slack.api.model.User> getAllWorkspaceUsers() throws IOException, SlackApiException {
        try {
            // Check if cache is valid
            long currentTime = System.currentTimeMillis();
            if (cachedUsers != null && (currentTime - lastUserFetchTime) < USER_CACHE_DURATION) {
                return cachedUsers;
            }
            
            log.debug("Getting all workspace users");
            
            MethodsClient methods = slack.methods(botToken);
            
            UsersListResponse response = methods.usersList(UsersListRequest.builder().build());
            
            if (!response.isOk()) {
                log.error("Failed to get users: {}", response.getError());
                throw new RuntimeException("Failed to get users: " + response.getError());
            }
            
            List<com.slack.api.model.User> members = response.getMembers();
            if (members == null) {
                log.warn("Received null members list from Slack API");
                return new ArrayList<>();
            }
            
            // Update cache before returning
            cachedUsers = members.stream()
                    .filter(user -> !user.isBot() && !user.isDeleted())
                    .collect(Collectors.toList());
            lastUserFetchTime = currentTime;
            
            return cachedUsers;
        } catch (SlackApiException e) {
            if (e.getResponse().code() == 429) {
                throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "You have reached the rate limit of Slack. Please wait a second and try again. Thank you for your patience."
                );
            }
            throw e;
        }
    }

    @Override
    public com.slack.api.model.User getSlackUserById(String userId) throws IOException, SlackApiException {
        try {
            log.debug("Getting Slack user by ID: {}", userId);
            
            MethodsClient methods = slack.methods(botToken);
            
            UsersListResponse response = methods.usersList(UsersListRequest.builder().build());
            
            if (!response.isOk()) {
                log.error("Failed to get users: {}", response.getError());
                throw new RuntimeException("Failed to get users: " + response.getError());
            }
            
            List<com.slack.api.model.User> members = response.getMembers();
            if (members == null) {
                log.warn("Received null members list from Slack API");
                throw new RuntimeException("Received null members list from Slack API");
            }
            
            Optional<com.slack.api.model.User> user = members.stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst();
            
            if (user.isEmpty()) {
                log.error("User not found with ID: {}", userId);
                throw new RuntimeException("User not found with ID: " + userId);
            }
            
            return user.get();
        } catch (SlackApiException e) {
            if (e.getResponse().code() == 429) {
                throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "You have reached the rate limit of Slack. Please wait a second and try again. Thank you for your patience."
                );
            }
            throw e;
        }
    }

    @Override
    public com.slack.api.model.User getSlackUserByUsername(String username) throws IOException, SlackApiException {
        try {
            log.debug("Getting Slack user by username: {}", username);
            
            MethodsClient methods = slack.methods(botToken);
            
            UsersListResponse response = methods.usersList(UsersListRequest.builder().build());
            
            if (!response.isOk()) {
                log.error("Failed to get users: {}", response.getError());
                throw new RuntimeException("Failed to get users: " + response.getError());
            }
            
            List<com.slack.api.model.User> members = response.getMembers();
            if (members == null) {
                log.warn("Received null members list from Slack API");
                throw new RuntimeException("Received null members list from Slack API");
            }
            
            // Remove @ if present in username
            String cleanUsername = username.startsWith("@") ? username.substring(1) : username;
            
            Optional<com.slack.api.model.User> user = members.stream()
                    .filter(u -> u.getName().equals(cleanUsername))
                    .findFirst();
            
            if (user.isEmpty()) {
                log.error("User not found with username: {}", username);
                throw new RuntimeException("User not found with username: " + username);
            }
            
            return user.get();
        } catch (SlackApiException e) {
            if (e.getResponse().code() == 429) {
                throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "You have reached the rate limit of Slack. Please wait a second and try again. Thank you for your patience."
                );
            }
            throw e;
        }
    }

    @Override
    public SlashCommandResponse buildSlashCommandResponse(String text) {
        return SlashCommandResponse.builder().text(text).responseType("ephemeral").build();
    }

    @Override
    public SlashCommandResponse buildErrorResponse(String error) {
        return SlashCommandResponse.builder().text(":x: " + error).responseType("ephemeral").build();
    }

    private <T> T callWithRetry(Callable<T> apiCall) throws IOException, SlackApiException {
        int maxRetries = 3;
        long retryDelayMs = 1000; // Start with 1 second
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return apiCall.call();
            } catch (SlackApiException e) {
                if (e.getResponse().code() == 429 && attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs * (long)Math.pow(2, attempt));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ResponseStatusException(
                            HttpStatus.TOO_MANY_REQUESTS,
                            "You have reached the rate limit of Slack. Please wait a second and try again. Thank you for your patience."
                        );
                    }
                } else if (e.getResponse().code() == 429) {
                    throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "You have reached the rate limit of Slack. Please wait a second and try again. Thank you for your patience."
                    );
                } else {
                    throw e;
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        throw new IOException("Max retries exceeded");
    }
} 