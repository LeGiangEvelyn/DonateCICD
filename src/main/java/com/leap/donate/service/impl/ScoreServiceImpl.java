package com.leap.donate.service.impl;

import com.leap.donate.model.CurrentScore;
import com.leap.donate.model.HistoryScore;
import com.leap.donate.model.RemainingPoints;
import com.leap.donate.model.User;
import com.leap.donate.repository.CurrentScoreRepository;
import com.leap.donate.repository.HistoryScoreRepository;
import com.leap.donate.repository.RemainingPointsRepository;
import com.leap.donate.repository.UserRepository;
import com.leap.donate.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final CurrentScoreRepository currentScoreRepository;
    private final RemainingPointsRepository remainingPointsRepository;
    private final HistoryScoreRepository historyScoreRepository;
    private final UserRepository userRepository;

    @Value("${app.max-points-per-month}")
    private Integer maxPointsPerMonth;

    @Override
    @Transactional
    public CurrentScore getCurrentScore(User user) {
        log.debug("Getting current coin for user: {}", user.getUsername());
        YearMonth currentYearMonth = YearMonth.now();
        return currentScoreRepository.findByUserAndMonthAndYear(
                        user, currentYearMonth.getMonthValue(), currentYearMonth.getYear())
                .orElseGet(() -> createCurrentScore(user, 0));
    }

    @Override
    @Transactional
    public RemainingPoints getRemainingPoints(User user) {
        log.debug("Getting remaining coins for user: {}", user.getUsername());
        YearMonth currentYearMonth = YearMonth.now();
        return remainingPointsRepository.findByUserAndMonthAndYear(
                        user, currentYearMonth.getMonthValue(), currentYearMonth.getYear())
                .orElseGet(() -> createRemainingPoints(user, maxPointsPerMonth));
    }

    @Override
    @Transactional
    public CurrentScore addPoints(User user, int points) {
        log.debug("Adding {} coins to user: {}", points, user.getUsername());
        CurrentScore currentScore = getCurrentScore(user);
        currentScore.setScore(currentScore.getScore() + points);
        currentScore.setUpdatedAt(LocalDateTime.now());
        return currentScoreRepository.save(currentScore);
    }

    @Override
    @Transactional
    public RemainingPoints deductPoints(User user, int points) {
        log.debug("Deducting {} coins from user: {}", points, user.getUsername());
        RemainingPoints remainingPoints = getRemainingPoints(user);
        
        if (remainingPoints.getRemainingPoints() < points) {
            throw new IllegalArgumentException("Not enough points remaining");
        }
        
        remainingPoints.setRemainingPoints(remainingPoints.getRemainingPoints() - points);
        remainingPoints.setUpdatedAt(LocalDateTime.now());
        return remainingPointsRepository.save(remainingPoints);
    }

    @Override
    @Transactional
    public void resetMonthlyScores() {
        log.info("Resetting monthly scores and remaining coins");
        
        // Get the previous month and year
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        int month = previousMonth.getMonthValue();
        int year = previousMonth.getYear();
        
        // Archive scores from the previous month
        archiveScores(month, year);
        
        // Create new remaining coins entries for all users
        userRepository.findAll().forEach(user -> {
            YearMonth currentYearMonth = YearMonth.now();
            // Check if user already has an entry for the current month
            if (remainingPointsRepository.findByUserAndMonthAndYear(
                    user, currentYearMonth.getMonthValue(), currentYearMonth.getYear()).isEmpty()) {
                createRemainingPoints(user, maxPointsPerMonth);
            }
        });
    }

    @Override
    @Transactional
    public void archiveScores(int month, int year) {
        log.info("Archiving scores for {}/{}", month, year);
        
        List<CurrentScore> scoresToArchive = currentScoreRepository.findAllByMonthAndYear(month, year);
        
        // Convert and save current scores to history
        scoresToArchive.forEach(score -> {
            historyScoreRepository.save(HistoryScore.builder()
                    .user(score.getUser())
                    .username(score.getUser().getUsername())
                    .score(score.getScore())
                    .month(score.getMonth())
                    .year(score.getYear())
                    .createdAt(LocalDateTime.now())
                    .build());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrentScore> getTopScores(int limit) {
        log.debug("Getting top {} scores", limit);
        YearMonth currentYearMonth = YearMonth.now();
        List<CurrentScore> allScores = currentScoreRepository.findAllByMonthAndYearOrderByScoreDesc(
                currentYearMonth.getMonthValue(), currentYearMonth.getYear());
        
        return allScores.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryScore> getUserHistory(User user) {
        log.debug("Getting score history for user: {}", user.getUsername());
        return historyScoreRepository.findAllByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CurrentScore> findCurrentScoreByUser(User user) {
        YearMonth currentYearMonth = YearMonth.now();
        return currentScoreRepository.findByUserAndMonthAndYear(
                user, currentYearMonth.getMonthValue(), currentYearMonth.getYear());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RemainingPoints> findRemainingPointsByUser(User user) {
        YearMonth currentYearMonth = YearMonth.now();
        return remainingPointsRepository.findByUserAndMonthAndYear(
                user, currentYearMonth.getMonthValue(), currentYearMonth.getYear());
    }

    @Override
    public List<CurrentScore> getTop10Scores(int month, int year) {
        log.debug("Getting top 10 scores for month {} and year {}", month, year);
        return currentScoreRepository.findTop10ByMonthAndYearOrderByScoreDesc(month, year);
    }

    private CurrentScore createCurrentScore(User user, int initialScore) {
        YearMonth currentYearMonth = YearMonth.now();
        CurrentScore currentScore = CurrentScore.builder()
                .user(user)
                .score(initialScore)
                .month(currentYearMonth.getMonthValue())
                .year(currentYearMonth.getYear())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return currentScoreRepository.save(currentScore);
    }

    private RemainingPoints createRemainingPoints(User user, int initialPoints) {
        YearMonth currentYearMonth = YearMonth.now();
        RemainingPoints remainingPoints = RemainingPoints.builder()
                .user(user)
                .remainingPoints(initialPoints)
                .month(currentYearMonth.getMonthValue())
                .year(currentYearMonth.getYear())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return remainingPointsRepository.save(remainingPoints);
    }
} 