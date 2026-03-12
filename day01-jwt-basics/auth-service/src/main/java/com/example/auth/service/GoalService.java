package com.example.auth.service;

import com.example.auth.dto.UpdatedGoalRequest;
import com.example.auth.model.Goal;
import com.example.auth.model.Stage;
import com.example.auth.model.User;
import com.example.auth.repository.GoalRepository;
import com.example.auth.repository.StageRepository;
import com.example.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
@Slf4j
public class GoalService {
    private final GoalRepository goalRepository;
    private final StageRepository stageRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public GoalService(GoalRepository goalRepository, StageRepository stageRepository, UserRepository userRepository, JwtService jwtService){
        this.goalRepository = goalRepository;
        this.stageRepository = stageRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;

    }

    public Page<Goal> getUserGoals(String token, int page, int size) {
        Long userId = jwtService.extractId(token);
        log.info("Fetching goals for userId={} page={} size={}", userId, page, size);

        // создаем настройку то как нам выдавать
        Pageable pageable = PageRequest.of(page,size,Sort.by("id"));

        return  goalRepository.findByUserId(userId, pageable);

         }

    @Transactional(readOnly = true)
    public Goal getGoalWithStages(String token, Long goalid){
        Long userId = jwtService.extractId(token);
        log.info("Fetching goal with stages for userId={} goalId={}", userId, goalid);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found when fetching goal with stages userId={}", userId);
                    return new IllegalArgumentException("Пользователь не найден");
                });

        Goal goal  = goalRepository.findByUserAndId(user,goalid).orElseThrow(() -> {
            log.warn("Goal not found for userId={} goalId={}", userId, goalid);
            return new IllegalArgumentException("Нет такой цели");
        });
       
        goal.getStages().size(); // чтобы подтянуть все задачи цели
        return goal;
    }

    @Transactional
    public Goal createGoal(String token, String description, String title, List<Stage> stages ){

        Long userId = jwtService.extractId(token);
        log.info("Create goal request for userId={} title={}", userId, title);
        if (goalRepository.findByUserIdAndTitle(userId, title).isPresent()){
            log.warn("Create goal failed: goal with same title already exists userId={} title={}", userId, title);
            throw new IllegalArgumentException("Цель с названием '" + title + "' уже существует");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Create goal failed: user not found userId={}", userId);
                    return new IllegalArgumentException("Пользователь не найден");
                });

        Goal goal = new Goal();
        goal.setUser(user);
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setStages(stages);

        Goal saved = goalRepository.save(goal);
        log.info("Goal created successfully id={} userId={}", saved.getId(), userId);
        return saved;
    }

    @Transactional
    public Goal UpdateGoal(String token , UpdatedGoalRequest request){
        Long userId = jwtService.extractId(token);
        log.info("Update goal request for userId={} goalId={}", userId, request.getGoalId());

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Update goal failed: user not found userId={}", userId);
            return new IllegalArgumentException("Нет такого юзера");
        });
        Goal goal = goalRepository.findByUserAndId(user, request.getGoalId())
                .orElseThrow(() -> {
                    log.warn("Update goal failed: goal not found userId={} goalId={}", userId, request.getGoalId());
                    return new IllegalArgumentException("Нет такой задачи");
                });
        if (request.getDescription()!=null) {
           goal.setDescription(request.getDescription());
        }
        if (request.getTitle() !=null){
            goal.setTitle(request.getTitle());
        }
        if (request.getStages()!=null){
          goal.setStages(request.getStages());
        }
        goal.recalculateProgress();

        Goal updated = goalRepository.save(goal);
        log.info("Goal updated successfully id={} userId={}", updated.getId(), userId);
        return  updated;
    }
    @Transactional
    public String  DeleteGoal(String token , Long goalID){

       Long userId =  jwtService.extractId(token);
        log.info("Delete goal request for userId={} goalId={}", userId, goalID);
        User user =  userRepository.findById(userId )
                .orElseThrow(() -> {
                    log.warn("Delete goal failed: user not found userId={}", userId);
                    return new IllegalArgumentException("такого юзера нет");
                });
        Goal goal = goalRepository.findByUserAndId(user, goalID).
                orElseThrow(() ->  {
                    log.warn("Delete goal failed: goal not found userId={} goalId={}", userId, goalID);
                    return new IllegalArgumentException(" Нет такой задачи ");
                });
        goalRepository.delete(goal);
        log.info("Goal deleted successfully id={} userId={}", goal.getId(), userId);
        return  ("Goal with ID:"+goal.getId()+" was deleted" );
    }



}
