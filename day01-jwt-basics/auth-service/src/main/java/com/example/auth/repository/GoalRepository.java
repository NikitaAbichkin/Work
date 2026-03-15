package com.example.auth.repository;

import com.example.auth.model.Goal;
import com.example.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    Page<Goal> findByUserId(Long userId, Pageable pageable);

    // условно делает лист из заданного колличества элементов  находит все обьекты где userId

    Optional<Goal> findByUserIdAndTitle(Long userId, String title);

    Optional<Goal> findByUserAndId(User user, Long id);

    @Query(
"select  g from Goal g where g.user.id =:userId "+
"and (:status is null or g.status = :status) "+
"and (:priority is null or g.priority = :priority)"
    )
    Page<Goal> findByFilters(
            @Param("userId") Long userId,
            @Param("status")Goal.GoalStatus status,
            @Param("priority")Goal.PriorityStatus priority,
            Pageable pageable
            );











}


