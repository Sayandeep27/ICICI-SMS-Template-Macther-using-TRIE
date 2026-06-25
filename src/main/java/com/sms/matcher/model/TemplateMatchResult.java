package com.sms.matcher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "template_match_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMatchResult {

    @Id
    private Long id;

    // Which template matched
    @Column(name = "template_id", nullable = false)
    private Long templateId;

    // The original incoming message
    @Column(name = "incoming_message", nullable = false)
    private String incomingMessage;

    // Extracted variable values stored as comma-separated string
    // Example: "4532,2"
    @Column(name = "extracted_values")
    private String extractedValues;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @PrePersist
    public void prePersist() {
        this.matchedAt = LocalDateTime.now();
    }
}
