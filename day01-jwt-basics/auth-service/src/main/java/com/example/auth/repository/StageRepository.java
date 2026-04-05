package com.example.auth.repository;

import com.example.auth.model.Goal;
import com.example.auth.model.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StageRepository extends JpaRepository<Stage, Long> {
    Optional<Stage> findByIdAndGoalId(Long id, Long goalId);


    Page<Stage> findAllByGoal_Id(Long goalId, Pageable pageable);


}
