package com.enterprise.backend.repository;

import com.enterprise.backend.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    // Fast lookup method to fetch flag configurations by their string key identifier
    Optional<FeatureFlag> findByFlagKey(String flagKey);

//    Dynamically fetches flags. If environmentId is null, it skips the filter and returns all records. If provided, it filters strictly by that environment.
    @Query("SELECT f FROM FeatureFlag f WHERE :environmentId IS NULL OR f.environmentId = :environmentId")
    List<FeatureFlag> findByEnvironmentOptional(@Param("environmentId") Long environmentId);
}
