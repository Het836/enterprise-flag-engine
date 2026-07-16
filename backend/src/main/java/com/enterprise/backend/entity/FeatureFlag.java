package com.enterprise.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

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

    @Column(name = "flag_key", nullable = false, unique = true)
    private String flagKey;

//    @Column(nullable = false)
//    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    // Maps directly to our JSONB column. We store it as a String
    // so our RuleEvaluator Jackson mapper can read it.
    @Column(name = "targeting_rules", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String targetingRules;

    @Column(name = "environment_id", nullable = false)
    private Long environmentId;

//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at", nullable = false)
//    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

//    @PrePersist
//    protected void onCreate(){
//        createdAt = LocalDateTime.now();
//        updatedAt = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate(){
//        updatedAt = LocalDateTime.now();
//    }

    // Explicit public getter to override any IDE compiler indexing lag
    public boolean isEnabled() {
        return this.isEnabled;
    }
}
