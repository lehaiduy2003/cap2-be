-- First drop the foreign key constraints if they exist
SET @constraint_name = (
    SELECT CONSTRAINT_NAME 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'messages' 
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    AND CONSTRAINT_NAME LIKE '%receiver_id%'
);

SET @sql = IF(@constraint_name IS NOT NULL, 
    CONCAT('ALTER TABLE messages DROP FOREIGN KEY ', @constraint_name),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_name = (
    SELECT CONSTRAINT_NAME 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'messages' 
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    AND CONSTRAINT_NAME LIKE '%sender_id%'
);

SET @sql = IF(@constraint_name IS NOT NULL, 
    CONCAT('ALTER TABLE messages DROP FOREIGN KEY ', @constraint_name),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_name = (
    SELECT CONSTRAINT_NAME 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'messages' 
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    AND CONSTRAINT_NAME LIKE '%conversation_id%'
);

SET @sql = IF(@constraint_name IS NOT NULL, 
    CONCAT('ALTER TABLE messages DROP FOREIGN KEY ', @constraint_name),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Make all foreign key columns nullable
ALTER TABLE messages MODIFY COLUMN receiver_id BIGINT NULL;
ALTER TABLE messages MODIFY COLUMN sender_id BIGINT NULL;
ALTER TABLE messages MODIFY COLUMN conversation_id BIGINT NULL;

-- Add type column if it doesn't exist
SET @dbname = DATABASE();
SET @tablename = "messages";
SET @columnname = "type";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE 
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  "SELECT 1",
  "ALTER TABLE messages ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'PRIVATE'"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Re-add the foreign key constraints with unique names
ALTER TABLE messages 
ADD CONSTRAINT FK_messages_receiver_id 
FOREIGN KEY (receiver_id) REFERENCES users(id);

ALTER TABLE messages 
ADD CONSTRAINT FK_messages_sender_id 
FOREIGN KEY (sender_id) REFERENCES users(id);

ALTER TABLE messages 
ADD CONSTRAINT FK_messages_conversation_id 
FOREIGN KEY (conversation_id) REFERENCES conversations(id); 