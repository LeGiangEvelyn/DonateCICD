package com.leap.donate.service;

import com.leap.donate.model.Transaction;
import com.leap.donate.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TransactionService {
    
    Transaction createTransaction(
        User sender, 
        User recipient, 
        int amount, 
        String message
    );
    
    Page<Transaction> getTransactionsBySender(User sender, Pageable pageable);
    
    Page<Transaction> getTransactionsByRecipient(User recipient, Pageable pageable);
    
    Page<Transaction> getTransactionsBySenderAndDateRange(
        User sender, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    
    Page<Transaction> getTransactionsByRecipientAndDateRange(
        User recipient, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    
    Page<Transaction> getTransactionsByDateRange(
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    
    boolean hasRecentTransactions(String userId, int count);
} 