package com.leap.donate.repository;

import com.leap.donate.model.Transaction;
import com.leap.donate.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Page<Transaction> findAllBySender(User sender, Pageable pageable);
    
    Page<Transaction> findAllByRecipient(User recipient, Pageable pageable);
    
    Page<Transaction> findAllBySenderAndCreatedAtBetween(
        User sender, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    
    Page<Transaction> findAllByRecipientAndCreatedAtBetween(
        User recipient, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    
    Page<Transaction> findAllByCreatedAtBetween(
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    
    long countBySenderIdAndCreatedAtAfter(String senderId, LocalDateTime createdAt);
} 