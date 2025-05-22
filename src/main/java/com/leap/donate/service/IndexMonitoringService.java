package com.leap.donate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexMonitoringService {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    public void monitorIndexUsage() {
        String sql = "SELECT " +
            "schemaname, " +
            "relname as tablename, " +
            "indexrelname as indexname, " +
            "idx_scan as index_scans, " +
            "idx_tup_read as tuples_read, " +
            "idx_tup_fetch as tuples_fetched, " +
            "pg_size_pretty(pg_relation_size(schemaname || '.' || indexrelname::text)) as index_size " +
            "FROM pg_stat_user_indexes " +
            "WHERE relname IN ('transactions', 'users', 'current_scores', 'remaining_points', 'history_scores') " +
            "ORDER BY idx_scan DESC";
            
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        
        log.info("Index Usage Report:");
        results.forEach(row -> 
            log.info("Table: {}, Index: {}, Scans: {}, Size: {}", 
                row.get("tablename"),
                row.get("indexname"),
                row.get("index_scans"),
                row.get("index_size"))
        );
    }
    
    public void analyzeSlowQueries() {
        String sql = "SELECT " +
            "query, " +
            "calls, " +
            "total_time, " +
            "mean_time, " +
            "rows " +
            "FROM pg_stat_statements " +
            "WHERE query LIKE '%transactions%' " +
            "ORDER BY mean_time DESC " +
            "LIMIT 10";
            
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        
        log.info("Slow Queries Report:");
        results.forEach(row -> 
            log.info("Query: {}, Mean Time: {}ms, Calls: {}", 
                row.get("query"),
                row.get("mean_time"),
                row.get("calls"))
        );
    }
} 