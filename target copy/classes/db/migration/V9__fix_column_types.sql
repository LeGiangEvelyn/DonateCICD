-- Drop foreign key constraints
ALTER TABLE IF EXISTS current_scores DROP CONSTRAINT IF EXISTS fk_current_scores_user;
ALTER TABLE IF EXISTS history_scores DROP CONSTRAINT IF EXISTS fk_history_scores_user;
ALTER TABLE IF EXISTS remaining_points DROP CONSTRAINT IF EXISTS fk_remaining_points_user;
ALTER TABLE IF EXISTS transactions DROP CONSTRAINT IF EXISTS fk_transactions_sender;
ALTER TABLE IF EXISTS transactions DROP CONSTRAINT IF EXISTS fk_transactions_recipient;

-- Drop primary key constraint
ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS users_pkey;

-- Change column types
ALTER TABLE users ALTER COLUMN id TYPE VARCHAR(255);
ALTER TABLE current_scores ALTER COLUMN user_id TYPE VARCHAR(255);
ALTER TABLE history_scores ALTER COLUMN user_id TYPE VARCHAR(255);
ALTER TABLE remaining_points ALTER COLUMN user_id TYPE VARCHAR(255);
ALTER TABLE transactions ALTER COLUMN sender_id TYPE VARCHAR(255);
ALTER TABLE transactions ALTER COLUMN recipient_id TYPE VARCHAR(255);

-- Re-add primary key constraint
ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id);

-- Re-add foreign key constraints
ALTER TABLE current_scores ADD CONSTRAINT fk_current_scores_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE history_scores ADD CONSTRAINT fk_history_scores_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE remaining_points ADD CONSTRAINT fk_remaining_points_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_sender FOREIGN KEY (sender_id) REFERENCES users(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_recipient FOREIGN KEY (recipient_id) REFERENCES users(id); 