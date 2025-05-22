package com.leap.donate.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Value("${app.rate-limit.requests-per-minute}")
    private int requestsPerMinute;

    @Bean
    public Bucket createNewBucket() {
        // Allow 10 requests per minute
        Bandwidth limit = Bandwidth.simple(requestsPerMinute, Duration.ofMinutes(1));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
} 