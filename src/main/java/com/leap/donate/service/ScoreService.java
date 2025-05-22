package com.leap.donate.service;

import com.leap.donate.model.CurrentScore;
import com.leap.donate.model.HistoryScore;
import com.leap.donate.model.RemainingPoints;
import com.leap.donate.model.User;

import java.util.List;
import java.util.Optional;

public interface ScoreService {
    CurrentScore getCurrentScore(User user);
    RemainingPoints getRemainingPoints(User user);
    CurrentScore addPoints(User user, int points);
    RemainingPoints deductPoints(User user, int points);
    void resetMonthlyScores();
    void archiveScores(int month, int year);
    List<CurrentScore> getTopScores(int limit);
    List<HistoryScore> getUserHistory(User user);
    Optional<CurrentScore> findCurrentScoreByUser(User user);
    Optional<RemainingPoints> findRemainingPointsByUser(User user);
    List<CurrentScore> getTop10Scores(int month, int year);
} 