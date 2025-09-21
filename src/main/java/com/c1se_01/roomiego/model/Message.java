package com.c1se_01.roomiego.model;

import com.c1se_01.roomiego.enums.MessageType;
import com.c1se_01.roomiego.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sender_id", nullable = true)
    private Long senderId;
    
    private String senderName;
    
    @Column(name = "receiver_id", nullable = true)
    private Long receiverId;
    
    private String receiverName;
    
    @Column(name = "conversation_id", nullable = true)
    private Long conversationId;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    private String media;
    
    private String mediaType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Long timestamp;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @ManyToOne
    @JoinColumn(name = "conversation_id", insertable = false, updatable = false)
    private Conversation conversation;

//    @ManyToOne
//    @JoinColumn(name = "sender_id", nullable = false)
//    private User sender;
//
//    @ManyToOne
//    @JoinColumn(name = "receiver_id", nullable = false)
//    private User receiver;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name = "sent_at", updatable = false)
//    private Date sentAt = new Date();
//
//    @ManyToOne
//    @JoinColumn(name = "conversation_id", nullable = false)
//    private Conversation conversation;

    public Message(String senderName, String receiverName, String message, String media, String mediaType, Status status, Long timestamp, MessageType type) {
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.message = message;
        this.media = media;
        this.mediaType = mediaType;
        this.status = status;
        this.timestamp = timestamp;
        this.type = type;
    }
}
