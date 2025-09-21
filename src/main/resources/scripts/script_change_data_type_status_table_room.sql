ALTER TABLE rooms ADD COLUMN is_room_available TINYINT(1);

SET SQL_SAFE_UPDATES = 0;

UPDATE rooms  
SET is_room_available = 
    CASE  
        WHEN `status` = 'AVAILABLE' THEN 1  
        ELSE 0  
    END  
WHERE id IS NOT NULL;

ALTER TABLE rooms DROP COLUMN `status`;