package com.leap.donate.repository;

import com.leap.donate.model.RemainingPoints;
import com.leap.donate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemainingPointsRepository extends JpaRepository<RemainingPoints, Long> {
    Optional<RemainingPoints> findByUserAndMonthAndYear(User user, Integer month, Integer year);
    List<RemainingPoints> findAllByMonthAndYear(Integer month, Integer year);
} 