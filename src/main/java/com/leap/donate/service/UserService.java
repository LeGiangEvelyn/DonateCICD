package com.leap.donate.service;

import com.leap.donate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    List<User> findAllUsers();
    List<User> findAllUsersExcept(String userId);
    User getOrCreateUserFromSlack(String userId, String username, String realName, String slackId);
    List<User> getAllUsers();
} 