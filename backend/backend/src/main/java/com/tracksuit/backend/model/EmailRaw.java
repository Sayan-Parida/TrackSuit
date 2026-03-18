package com.tracksuit.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_raw")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(nullable = false, length = 500)
    private String subject;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String body;

    @Column(name = "parsed_flag", nullable = false)
    private Boolean parsedFlag = false;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
