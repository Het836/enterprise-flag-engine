package com.enterprise.backend.repository;

import com.enterprise.backend.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    // Add this line to handle the API key lookup constraint
    Optional<Environment> findByApiKey(String apiKey);

}
