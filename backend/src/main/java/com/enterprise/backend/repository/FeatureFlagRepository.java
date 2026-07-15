package com.enterprise.backend.repository;

import com.enterprise.backend.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    // Fast lookup method to fetch flag configurations by their string key identifier
    Optional<FeatureFlag> findByKey(String key);
}
