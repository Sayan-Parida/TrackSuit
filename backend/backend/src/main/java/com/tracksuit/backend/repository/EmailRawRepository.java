package com.tracksuit.backend.repository;

import com.tracksuit.backend.model.EmailRaw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRawRepository extends JpaRepository<EmailRaw, Long> {
    boolean existsByMessageId(String messageId);
}
