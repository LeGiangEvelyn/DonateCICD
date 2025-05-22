-- First, drop all foreign key constraints that reference the users table
ALTER TABLE current_scores DROP CONSTRAINT IF EXISTS current_scores_user_id_fkey;
ALTER TABLE history_scores DROP CONSTRAINT IF EXISTS history_scores_user_id_fkey;
ALTER TABLE remaining_points DROP CONSTRAINT IF EXISTS remaining_points_user_id_fkey;
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_sender_id_fkey;
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_recipient_id_fkey;

-- Drop the primary key constraint
ALTER TABLE users DROP CONSTRAINT users_pkey;

-- Change the column type to VARCHAR(255)
ALTER TABLE users ALTER COLUMN id TYPE VARCHAR(255);

-- Add back the primary key constraint
ALTER TABLE users ADD PRIMARY KEY (id);

-- Re-add the foreign key constraints
ALTER TABLE current_scores ADD CONSTRAINT current_scores_user_id_fkey 
    FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE history_scores ADD CONSTRAINT history_scores_user_id_fkey 
    FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE remaining_points ADD CONSTRAINT remaining_points_user_id_fkey 
    FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE transactions ADD CONSTRAINT transactions_sender_id_fkey 
    FOREIGN KEY (sender_id) REFERENCES users(id);
ALTER TABLE transactions ADD CONSTRAINT transactions_recipient_id_fkey 
    FOREIGN KEY (recipient_id) REFERENCES users(id);
