package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.model.Conversation;
import com.c1se_01.roomiego.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
        SELECT c FROM Conversation c
        WHERE (c.user1.id = :user1Id AND c.user2.id = :user2Id)
        OR (c.user1.id = :user2Id AND c.user2.id = :user1Id)
    """)
    Optional<Conversation> findByUser1IdAndUser2Id(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

//    @Query("SELECT c FROM Conversation c WHERE c.user1 = :user OR c.user2 = :user")
//    List<Conversation> findAllByUser(@Param("user") User user);
}
