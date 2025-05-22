package com.leap.donate.service.impl;

import com.leap.donate.model.Transaction;
import com.leap.donate.model.User;
import com.leap.donate.repository.TransactionRepository;
import com.leap.donate.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Transaction createTransaction(
        User sender, 
        User recipient, 
        int amount, 
        String message
    ) {
        log.debug(
            "Creating transaction: sender={}, recipient={}, amount={}",
            sender.getUsername(), 
            recipient.getUsername(), 
            amount
        );
        
        LocalDateTime now = LocalDateTime.now();
      
        Transaction transaction = Transaction.builder()
            .sender(sender)
            .recipient(recipient)
            .amount(amount)
            .message(message)
            .createdAt(now)
            .build();
        
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsBySender(User sender, Pageable pageable) {
        log.debug("Getting transactions by sender: {}", sender.getUsername());
        return transactionRepository.findAllBySender(sender, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByRecipient(User recipient, Pageable pageable) {
        log.debug("Getting transactions by recipient: {}", recipient.getUsername());
        return transactionRepository.findAllByRecipient(recipient, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsBySenderAndDateRange(
        User sender, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    ) {
        log.debug(
            "Getting transactions by sender and date range: {}, from {} to {}", 
            sender.getUsername(), 
            start, 
            end
        );
        return transactionRepository.findAllBySenderAndCreatedAtBetween(sender, start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByRecipientAndDateRange(
        User recipient, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    ) {
        log.debug(
            "Getting transactions by recipient and date range: {}, from {} to {}", 
            recipient.getUsername(), 
            start, 
            end
        );
        return transactionRepository.findAllByRecipientAndCreatedAtBetween(recipient, start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByDateRange(
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    ) {
        log.debug("Getting transactions by date range: from {} to {}", start, end);
        return transactionRepository.findAllByCreatedAtBetween(start, end, pageable);
    }

    @Override
    public boolean hasRecentTransactions(String userId, int count) {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(5);
        return transactionRepository.countBySenderIdAndCreatedAtAfter(userId, recentTime) >= count;
    }
} 