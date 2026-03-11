package com.example.auth.service;

import com.example.auth.dto.UpdatedStage;
import com.example.auth.model.Goal;
import com.example.auth.model.User;
import com.example.auth.repository.GoalRepository;
import com.example.auth.repository.StageRepository;
import com.example.auth.model.Stage;

import java.time.LocalDateTime;

import com.example.auth.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service

public class StageService {

    private final StageRepository stageRepository;
    private final JwtService jwtService;
    private final GoalRepository goalRepository;
    private  final UserRepository userRepository;

    public StageService(StageRepository stageRepository, JwtService jwtService, GoalRepository goalRepository,UserRepository userRepository){
        this.stageRepository = stageRepository;
        this.jwtService = jwtService;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public Stage addStage(String token, Long goalId, String title, String description, String priority, String estimatedTime, LocalDateTime deadline, LocalDateTime startsAt, Integer order ){
        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("Нет такого пользователя"));
        Goal goal  = goalRepository.
                findByUserAndId(user,goalId).orElseThrow(()-> new IllegalArgumentException(" нет такой цели"));




        Stage stage  = new Stage();
        stage.setGoal(goal);
        stage.setDeadline(deadline);
        stage.setDescription(description);
        stage.setEstimatedTime(estimatedTime);
        stage.setPriority(priority);
        stage.setTitle(title);
        stage.setStartsAt(startsAt);
        stage.setSortOrder(order);

        stageRepository.save(stage);

        return  stage;
    }

    @Transactional
    public Stage updateStage(String token, UpdatedStage updatedStage){
        if (updatedStage == null || updatedStage.getStageId() == null || updatedStage.getGoalId() == null) {
            throw new IllegalArgumentException("stageId и goalId обязательны");
        }
        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("Нет такого пользователя"));
        Goal goal  = goalRepository.
                findByUserAndId(user, updatedStage.getGoalId()).orElseThrow(()-> new IllegalArgumentException(" нет такой цели"));

        Long goalId = goal.getId();
        Long stageId = updatedStage.getStageId();
        Stage stage = stageRepository.findByIdAndGoalId(stageId, goalId)
                .orElseThrow(()-> new IllegalArgumentException("Нет такой задачи"));

        if (updatedStage.getTitle() != null) {
            stage.setTitle(updatedStage.getTitle());
        }
        if (updatedStage.getDescription() != null) {
            stage.setDescription(updatedStage.getDescription());
        }
        if (updatedStage.getPriority() != null) {
            stage.setPriority(updatedStage.getPriority());
        }
        if (updatedStage.getEstimatedTime() != null) {
            stage.setEstimatedTime(updatedStage.getEstimatedTime());
        }
        if (updatedStage.getDeadline() != null) {
            stage.setDeadline(updatedStage.getDeadline());
        }
        if (updatedStage.getStartsAt() != null) {
            stage.setStartsAt(updatedStage.getStartsAt());
        }
        if (updatedStage.getProgress() != null) {
            stage.setProgress(updatedStage.getProgress());
        }
        if (updatedStage.getStatus() != null) {
            stage.setStatus(updatedStage.getStatus());
        }

        Stage saved = stageRepository.save(stage);
        goal.recalculateProgress();
        goalRepository.save(goal);
        return saved;
    }

    public String DeleteStage (String token,Long goalId, Long stageid){
        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("Нет такого пользователя"));
        Goal goal  = goalRepository.
                findByUserAndId(user, goalId).orElseThrow(()-> new IllegalArgumentException(" нет такой цели"));
        Stage stage = stageRepository.findByIdAndGoalId(stageid, goal.getId())
                .orElseThrow(()-> new IllegalArgumentException("Нет такой задачи"));

        stageRepository.delete(stage);

        return "Задача с id " + stage.getId() + " была удалена";
    }
}