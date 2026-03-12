package com.example.auth.repository;

import com.example.auth.model.Goal;
import com.example.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    Page<Goal> findByUserId(Long userId, Pageable pageable);

    // условно делает лист из заданного колличества элементов  находит все обьекты где userId

    Optional<Goal> findByUserIdAndTitle(Long userId, String title);

    Optional<Goal> findByUserAndId(User user, Long id);




}


