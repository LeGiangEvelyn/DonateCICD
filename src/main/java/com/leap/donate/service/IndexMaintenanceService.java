package com.leap.donate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexMaintenanceService {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void performMaintenance() {
        log.info("Starting index maintenance");
        
        // Vacuum analyze all tables
        vacuumAnalyzeTables();
        
        // Check for index bloat
        checkIndexBloat();
        
        // Rebuild indexes if needed
        rebuildIndexesIfNeeded();
        
        log.info("Index maintenance completed");
    }
    
    private void vacuumAnalyzeTables() {
        String[] tables = {
            "transactions", "users", "current_scores", 
            "remaining_points", "history_scores"
        };
        
        for (String table : tables) {
            try {
                jdbcTemplate.execute("VACUUM ANALYZE " + table);
                log.info("VACUUM ANALYZE completed for table: {}", table);
            } catch (Exception e) {
                log.error("Error during VACUUM ANALYZE for table {}: {}", table, e.getMessage());
            }
        }
    }
    
    private void checkIndexBloat() {
        String sql = "SELECT " +
            "schemaname, " +
            "tablename, " +
            "indexname, " +
            "pg_size_pretty(pg_relation_size(schemaname || '.' || indexname::text)) as index_size, " +
            "pg_size_pretty(pg_relation_size(schemaname || '.' || tablename::text)) as table_size " +
            "FROM pg_stat_user_indexes " +
            "WHERE tablename IN ('transactions', 'users', 'current_scores', 'remaining_points', 'history_scores') " +
            "ORDER BY pg_relation_size(schemaname || '.' || indexname::text) DESC";
            
        jdbcTemplate.queryForList(sql).forEach(row -> 
            log.info("Index: {}, Size: {}, Table Size: {}", 
                row.get("indexname"),
                row.get("index_size"),
                row.get("table_size"))
        );
    }
    
    private void rebuildIndexesIfNeeded() {
        String sql = "SELECT " +
            "schemaname, " +
            "tablename, " +
            "indexname " +
            "FROM pg_stat_user_indexes " +
            "WHERE tablename IN ('transactions', 'users', 'current_scores', 'remaining_points', 'history_scores') " +
            "AND idx_scan = 0 " +
            "AND pg_relation_size(schemaname || '.' || indexname::text) > 1000000"; // 1MB
            
        jdbcTemplate.queryForList(sql).forEach(row -> {
            String indexName = row.get("schemaname") + "." + row.get("indexname");
            try {
                jdbcTemplate.execute("REINDEX INDEX " + indexName);
                log.info("Rebuilt unused index: {}", indexName);
            } catch (Exception e) {
                log.error("Error rebuilding index {}: {}", indexName, e.getMessage());
            }
        });
    }
} 