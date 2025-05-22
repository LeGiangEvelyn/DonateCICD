package com.leap.donate.service;

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.slack.api.methods.SlackApiException;

import java.io.IOException;

public interface DonateService {
    SlashCommandResponse givePoints(String userId, String text, String channelId) throws IOException, SlackApiException;
    SlashCommandResponse showUserInfo(String userId, String channelId) throws IOException, SlackApiException;
    SlashCommandResponse showHelp() throws IOException, SlackApiException;
    SlashCommandResponse showTopTen(String channelId) throws IOException, SlackApiException;
} 