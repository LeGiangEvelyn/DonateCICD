CREATE TABLE users (
    id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    real_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE current_scores (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL REFERENCES users(id),
    score INTEGER NOT NULL DEFAULT 0,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, month, year)
);

CREATE TABLE remaining_points (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL REFERENCES users(id),
    remaining_points INTEGER NOT NULL,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, month, year)
);

CREATE TABLE history_scores (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL REFERENCES users(id),
    username VARCHAR(100) NOT NULL,
    score INTEGER NOT NULL,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    sender_id VARCHAR(20) NOT NULL REFERENCES users(id),
    recipient_id VARCHAR(20) NOT NULL REFERENCES users(id),
    amount INTEGER NOT NULL,
    message TEXT,
    is_jackpot BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    month INTEGER NOT NULL,
    year INTEGER NOT NULL
);

CREATE INDEX idx_transactions_sender ON transactions(sender_id);
CREATE INDEX idx_transactions_recipient ON transactions(recipient_id);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp);
CREATE INDEX idx_transactions_month_year ON transactions(month, year);
CREATE INDEX idx_current_scores_month_year ON current_scores(month, year);
CREATE INDEX idx_remaining_points_month_year ON remaining_points(month, year);
CREATE INDEX idx_history_scores_month_year ON history_scores(month, year); 