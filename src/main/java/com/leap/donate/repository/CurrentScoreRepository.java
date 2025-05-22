package com.leap.donate.repository;

import com.leap.donate.model.CurrentScore;
import com.leap.donate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrentScoreRepository extends JpaRepository<CurrentScore, Long> {
    Optional<CurrentScore> findByUserAndMonthAndYear(User user, Integer month, Integer year);
    List<CurrentScore> findAllByMonthAndYear(Integer month, Integer year);
    List<CurrentScore> findAllByMonthAndYearOrderByScoreDesc(Integer month, Integer year);
    Optional<CurrentScore> findByUserId(Long userId);
    List<CurrentScore> findTop10ByMonthAndYearOrderByScoreDesc(Integer month, Integer year);
} 