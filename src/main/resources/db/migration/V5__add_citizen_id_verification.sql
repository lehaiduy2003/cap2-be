-- Add citizen ID verification fields to users table
ALTER TABLE users
ADD COLUMN is_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN citizen_id_number VARCHAR(20) UNIQUE,
ADD COLUMN verification_date TIMESTAMP;

-- Create index on citizen_id_number for faster lookups
CREATE INDEX idx_users_citizen_id_number ON users(citizen_id_number);

-- Create index on is_verified for filtering verified users
CREATE INDEX idx_users_is_verified ON users(is_verified);
