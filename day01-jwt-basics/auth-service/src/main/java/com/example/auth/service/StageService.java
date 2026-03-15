package com.example.auth.service;

import com.example.auth.dto.UpdatedStage;
import com.example.auth.exception.GoalNotFoundException;
import com.example.auth.exception.StageNotFoundException;
import com.example.auth.exception.UserNotFoundException;
import com.example.auth.model.Goal;
import com.example.auth.model.User;
import com.example.auth.repository.GoalRepository;
import com.example.auth.repository.StageRepository;
import com.example.auth.model.Stage;
import com.example.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
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
    public Stage addStage(String token, Long goalId, String title, String description, String priority, Integer estimatedMinutes, LocalDate deadline, LocalDate startsAt, Integer order ){
        Long userId = jwtService.extractId(token);
        log.info("Add stage request for userId={} goalId={} title={}", userId, goalId, title);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Add stage failed: user not found userId={}", userId);
                    return new UserNotFoundException();
                });
        Goal goal  = goalRepository.
                findByUserAndId(user,goalId).orElseThrow(() -> {
                    log.warn("Add stage failed: goal not found userId={} goalId={}", userId, goalId);
                    return new GoalNotFoundException(goalId);
                });




        Stage stage  = new Stage();
        stage.setGoal(goal);
        stage.setDeadline(deadline);
        stage.setDescription(description);
        stage.setEstimatedMinutes(estimatedMinutes);
        if (priority != null) {
            stage.setPriority(Stage.PriorityStage.valueOf(priority.toUpperCase()));
        }
        stage.setTitle(title);
        stage.setStartsAt(startsAt);
        stage.setSortOrder(order);

        Stage saved = stageRepository.save(stage);
        log.info("Stage created successfully id={} goalId={} userId={}", saved.getId(), goalId, userId);

        return  saved;
    }

    @Transactional
    public Stage updateStage(String token, UpdatedStage updatedStage){
        if (updatedStage == null || updatedStage.getStageId() == null || updatedStage.getGoalId() == null) {
            log.warn("Update stage failed: missing required ids in request");
            throw new IllegalArgumentException("stageId и goalId обязательны");
        }
        Long userId = jwtService.extractId(token);
        log.info("Update stage request for userId={} goalId={} stageId={}", userId, updatedStage.getGoalId(), updatedStage.getStageId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Update stage failed: user not found userId={}", userId);
                    return new UserNotFoundException();
                });
        Long requestGoalId = updatedStage.getGoalId();
        Goal goal  = goalRepository.
                findByUserAndId(user, requestGoalId).orElseThrow(() -> {
                    log.warn("Update stage failed: goal not found userId={} goalId={}", userId, requestGoalId);
                    return new GoalNotFoundException(requestGoalId);
                });

        Long goalId = goal.getId();
        Long stageId = updatedStage.getStageId();
        Stage stage = stageRepository.findByIdAndGoalId(stageId, goalId)
                .orElseThrow(() -> {
                    log.warn("Update stage failed: stage not found goalId={} stageId={}", goalId, stageId);
                    return new StageNotFoundException(stageId);
                });

        if (updatedStage.getTitle() != null) {
            stage.setTitle(updatedStage.getTitle());
        }
        if (updatedStage.getDescription() != null) {
            stage.setDescription(updatedStage.getDescription());
        }
        if (updatedStage.getPriority() != null) {
            stage.setPriority(Stage.PriorityStage.valueOf(updatedStage.getPriority().toUpperCase()));
        }
        if (updatedStage.getEstimatedMinutes() != null) {
            stage.setEstimatedMinutes(updatedStage.getEstimatedMinutes());
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
            stage.setStatus(Stage.StatusPriority.valueOf(updatedStage.getStatus().toUpperCase()));
        }

        Stage saved = stageRepository.save(stage);
        goal.recalculateProgress();
        goalRepository.save(goal);
        log.info("Stage updated successfully id={} goalId={} userId={}", saved.getId(), goalId, userId);
        return saved;
    }

    public String DeleteStage (String token,Long goalId, Long stageid){
        Long userId = jwtService.extractId(token);
        log.info("Delete stage request for userId={} goalId={} stageId={}", userId, goalId, stageid);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Delete stage failed: user not found userId={}", userId);
                    return new UserNotFoundException();
                });
        Goal goal  = goalRepository.
                findByUserAndId(user, goalId).orElseThrow(() -> {
                    log.warn("Delete stage failed: goal not found userId={} goalId={}", userId, goalId);
                    return new GoalNotFoundException(goalId);
                });
        Stage stage = stageRepository.findByIdAndGoalId(stageid, goal.getId())
                .orElseThrow(() -> {
                    log.warn("Delete stage failed: stage not found goalId={} stageId={}", goalId, stageid);
                    return new StageNotFoundException(stageid);
                });

        stageRepository.delete(stage);
        log.info("Stage deleted successfully id={} goalId={} userId={}", stage.getId(), goalId, userId);

        return "Задача с id " + stage.getId() + " была удалена";
    }
}