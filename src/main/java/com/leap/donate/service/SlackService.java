package com.leap.donate.service;

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.User;

import java.io.IOException;
import java.util.List;

public interface SlackService {
    void postMessage(String channelId, String message) throws IOException, SlackApiException;
    void postEphemeralMessage(String channelId, String userId, String message) throws IOException, SlackApiException;
    String createOrGetDonateChannel() throws IOException, SlackApiException;
    void addUsersToDonateChannel(String channelId) throws IOException, SlackApiException;
    List<User> getAllWorkspaceUsers() throws IOException, SlackApiException;
    User getSlackUserById(String userId) throws IOException, SlackApiException;
    User getSlackUserByUsername(String username) throws IOException, SlackApiException;
    SlashCommandResponse buildSlashCommandResponse(String text);
    SlashCommandResponse buildErrorResponse(String error);
} 