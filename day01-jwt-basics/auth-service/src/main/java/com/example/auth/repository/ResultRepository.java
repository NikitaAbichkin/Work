package com.example.auth.repository;

import com.example.auth.model.Goal;
import com.example.auth.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findAllByGoal_Id(Long goalId);

    Optional<Result> findByIdAndGoal_Id(Long id, Long goalId);

    Long goal(Goal goal);
}
