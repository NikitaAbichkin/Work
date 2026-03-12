package com.example.auth.repository;

import com.example.auth.model.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StageRepository extends JpaRepository<Stage, Long> {
    Optional<Stage> findByIdAndGoalId(Long id, Long goalId);
}
