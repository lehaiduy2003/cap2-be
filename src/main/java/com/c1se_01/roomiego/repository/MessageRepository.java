package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.model.Message;
import com.c1se_01.roomiego.enums.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderName = :user1 AND m.receiverName = :user2) OR " +
            "(m.senderName = :user2 AND m.receiverName = :user1) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findChatHistoryBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

    List<Message> findByType(MessageType type);

    List<Message> findBySenderName(String senderName);
}
