package com.leap.donate.service.impl;

import com.leap.donate.model.User;
import com.leap.donate.repository.UserRepository;
import com.leap.donate.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsersExcept(String userId) {
        return userRepository.findAllExcept(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User getOrCreateUserFromSlack(String userId, String username, String realName, String slackId) {
        log.debug("Getting or creating user from Slack: ID={}, username={}", slackId, username);
        
        // First try to find by slackId
        Optional<User> existingUser = userRepository.findBySlackId(slackId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user information if needed
            if (!username.equals(user.getUsername()) || 
                (realName != null && !realName.equals(user.getRealName()))) {
                user.setUsername(username);
                user.setRealName(realName);
                return userRepository.save(user);
            }
            return user;
        }

        // Create new user if not found
        User newUser = User.builder()
            .id(UUID.randomUUID().toString())
            .slackId(slackId)
            .username(username)
            .realName(realName)
            .status(User.UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();

        return userRepository.save(newUser);
    }
} 