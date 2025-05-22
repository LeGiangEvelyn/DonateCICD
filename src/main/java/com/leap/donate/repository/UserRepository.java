package com.leap.donate.repository;

import com.leap.donate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findBySlackId(String slackId);
    
    @Query("SELECT u FROM User u WHERE u.id != :userId")
    List<User> findAllExcept(String userId);
} 