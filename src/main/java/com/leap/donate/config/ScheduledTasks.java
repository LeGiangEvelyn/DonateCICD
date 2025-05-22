package com.leap.donate.config;

import com.leap.donate.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final ScoreService scoreService;

    /**
     * Reset scores and remaining points at the beginning of each month.
     * Runs at 00:00:00 on the 1st day of every month.
     */
    @Scheduled(cron = "${app.reset-cron}")
    public void resetMonthlyScores() {
        log.info("Running scheduled task: resetMonthlyScores");
        scoreService.resetMonthlyScores();
    }
} 