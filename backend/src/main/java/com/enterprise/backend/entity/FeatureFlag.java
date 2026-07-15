package com.enterprise.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feature_flags")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeatureFlag{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    // Maps directly to our JSONB column. We store it as a String
    // so our RuleEvaluator Jackson mapper can read it.
    @Column(name = "targeting_rules", columnDefinition = "jsonb")
    private String targetingRules;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }

    // Explicit public getter to override any IDE compiler indexing lag
    public boolean isEnabled() {
        return this.isEnabled;
    }
}
