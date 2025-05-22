-- Rename timestamp column to createdAt
ALTER TABLE transactions RENAME COLUMN timestamp TO created_at;

-- Remove three columns (is_jackpot, month, and year)
ALTER TABLE transactions DROP COLUMN is_jackpot;
ALTER TABLE transactions DROP COLUMN month;
ALTER TABLE transactions DROP COLUMN year;

-- Update the index name to match the new column name
DROP INDEX IF EXISTS idx_transactions_timestamp;
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Keep only essential indexes
-- Transactions (core functionality)
CREATE INDEX IF NOT EXISTS idx_transactions_sender_created ON transactions(sender_id, created_at);
CREATE INDEX IF NOT EXISTS idx_transactions_recipient_created ON transactions(recipient_id, created_at);

-- Users (Slack integration)
CREATE INDEX IF NOT EXISTS idx_users_slack_id ON users(slack_id);

-- Current Scores (monthly calculations)
CREATE INDEX IF NOT EXISTS idx_current_scores_user_month_year ON current_scores(user_id, month, year); 