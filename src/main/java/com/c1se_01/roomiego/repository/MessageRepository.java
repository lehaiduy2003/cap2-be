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
                        "(m.senderId = :user1Id AND m.receiverId = :user2Id) OR " +
                        "(m.senderId = :user2Id AND m.receiverId = :user1Id) " +
                        "ORDER BY m.timestamp ASC")
        List<Message> findChatHistoryBetweenUsers(
                        @Param("user1Id") Long user1Id,
                        @Param("user2Id") Long user2Id);

        List<Message> findByType(MessageType type);

        List<Message> findBySenderId(Long senderId);

        Message findTopByConversationIdOrderByTimestampDesc(Long conversationId);

        @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.timestamp ASC")
        List<Message> findByConversationIdOrderByTimestampAsc(@Param("conversationId") Long conversationId);
}
