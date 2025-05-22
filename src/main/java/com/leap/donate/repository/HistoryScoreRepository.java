package com.leap.donate.repository;

import com.leap.donate.model.HistoryScore;
import com.leap.donate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryScoreRepository extends JpaRepository<HistoryScore, Long> {
    List<HistoryScore> findAllByUser(User user);
    List<HistoryScore> findAllByUserAndMonthAndYear(User user, Integer month, Integer year);
    List<HistoryScore> findAllByMonthAndYearOrderByScoreDesc(Integer month, Integer year);
} 