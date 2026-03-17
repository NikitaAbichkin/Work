package com.example.auth.service;

import com.example.auth.dto.CreateGoalRequest;
import com.example.auth.dto.ParametersForSearching;
import com.example.auth.dto.StageCreateRequest;
import com.example.auth.dto.UpdatedGoalRequest;
import com.example.auth.exception.GoalNotFoundException;
import com.example.auth.exception.UserNotFoundException;
import com.example.auth.model.Goal;
import com.example.auth.model.Stage;
import com.example.auth.model.User;
import com.example.auth.repository.GoalRepository;
import com.example.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GoalService {
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public GoalService(GoalRepository goalRepository, UserRepository userRepository, JwtService jwtService){
        this.goalRepository = goalRepository;
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
                    return new UserNotFoundException();
                });

        Goal goal  = goalRepository.findByUserAndId(user,goalid).orElseThrow(() -> {
            log.warn("Goal not found for userId={} goalId={}", userId, goalid);
            return new GoalNotFoundException(goalid);
        });
       
        goal.getStages().size(); // чтобы подтянуть все задачи цели
        return goal;
    }

    @Transactional
    public Goal createGoal(String token, CreateGoalRequest request){

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Название цели не может быть пустым");
        }
        if (request.getPriority() == null ||
                (!request.getPriority().equalsIgnoreCase("LOW")
                        && !request.getPriority().equalsIgnoreCase("MEDIUM")
                        && !request.getPriority().equalsIgnoreCase("HIGH"))) {
            throw new IllegalArgumentException("Приоритет должен быть LOW, MEDIUM или HIGH");
        }
        if (request.getStartdate() == null) {
            throw new IllegalArgumentException("Дата начала обязательна");
        }
        if (request.getStartdate()!=null && request.getDeadline()!=null && request.getStartdate().isAfter(request.getDeadline())){
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
        if (request.getDaily_time_minutes()!=null && request.getDaily_time_minutes()<0){
            throw new IllegalArgumentException("Ежедневное время не может быть отрицательным");
        }
        if (request.getStages()!=null && request.getStages().size()==0){
            throw new IllegalArgumentException("Список задач не может быть пустым");
        }
        if (request.getStages()!=null && request.getStages().size()>0){
            for (Stage stage : request.getStagesFromDto(request.getStages())){
                if (stage.getEstimatedMinutes()!=null && (stage.getEstimatedMinutes())<0){
                    throw new IllegalArgumentException("Время на выполнение задачи не может быть отрицательным");
                }
                if (stage.getDeadline()!=null && stage.getStartsAt() != null && stage.getDeadline().isBefore(stage.getStartsAt())){
                    throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
                }
            }
        }
        

        Long userId = jwtService.extractId(token);
        log.info("Create goal request for userId={} title={}", userId, request.getTitle());
        if (goalRepository.findByUserIdAndTitle(userId, request.getTitle()).isPresent()){
            log.warn("Create goal failed: goal with same title already exists userId={} title={}", userId,request.getTitle());
            throw new IllegalArgumentException("Цель с названием '" + request.getTitle() + "' уже существует");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Create goal failed: user not found userId={}", userId);
                    return new UserNotFoundException();
                });

                
                
        Goal goal = new Goal();
        goal.setUser(user);
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription()!=null?request.getDescription():"");
        goal.setStages(request.getStagesFromDto( request.getStages()));
        goal.setPriority(Goal.PriorityStatus.valueOf(request.getPriority().toUpperCase()));
        
        goal.setStartdate(request.getStartdate());
        goal.setDeadline(request.getDeadline());
        goal.setDaily_time_minutes(request.getDaily_time_minutes());
        goal.setStatus(Goal.GoalStatus.IN_PROGRESS);
        goal.setProgress(0);

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
            return new UserNotFoundException();
        });
        Goal goal = goalRepository.findByUserAndId(user, request.getGoalId())
                .orElseThrow(() -> {
                    log.warn("Update goal failed: goal not found userId={} goalId={}", userId, request.getGoalId());
                    return new GoalNotFoundException(request.getGoalId());
                });
        if (request.getDescription()!=null) {
           goal.setDescription(request.getDescription());
        }
        if (request.getTitle() !=null){
            goal.setTitle(request.getTitle());
        }
        if (request.getPriority() != null) {
            if (!request.getPriority().equalsIgnoreCase("LOW")
                    && !request.getPriority().equalsIgnoreCase("MEDIUM")
                    && !request.getPriority().equalsIgnoreCase("HIGH")) {
                throw new IllegalArgumentException("Приоритет должен быть LOW, MEDIUM или HIGH");
            }
            goal.setPriority(Goal.PriorityStatus.valueOf(request.getPriority().toUpperCase()));
        }
        if (request.getDaily_time_minutes() != null) {
            if (request.getDaily_time_minutes() < 0) {
                throw new IllegalArgumentException("Ежедневное время не может быть отрицательным");
            }
            goal.setDaily_time_minutes(request.getDaily_time_minutes());
        }
        if (request.getStartdate() != null) {
            if (goal.getDeadline() != null && request.getStartdate().isAfter(goal.getDeadline())) {
                throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
            }
            goal.setStartdate(request.getStartdate());
        }
        if (request.getDeadline() != null) {
            if (goal.getStartdate() != null && goal.getStartdate().isAfter(request.getDeadline())) {
                throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
            }
            goal.setDeadline(request.getDeadline());
        }

        if (request.getStages()!=null){
            List<StageCreateRequest> stages = request.getStages();
            List<Stage> stagesFinal = new ArrayList<>();
            for (StageCreateRequest s : stages){
                Stage stage = new Stage();
                stage.setTitle(s.getTitle());
                stage.setDescription(s.getDescription());
                if (s.getPriority() != null) {
                    try {
                        stage.setPriority(Stage.PriorityStage.valueOf(s.getPriority().toUpperCase()));
                    }
                    catch (Exception e){
                         throw  new IllegalArgumentException("Невалидный приоритет ");
                    }
                }
                if (s.getStatus()!=null){
                    try {
                        stage.setStatus(Stage.StatusPriority.valueOf(s.getStatus().toUpperCase()));

                    }
                    catch (Exception e){
                        throw  new IllegalArgumentException("Невалидный cтатус ");
                    }

                }


                stage.setEstimatedMinutes(s.getEstimatedMinutes());
                stage.setDeadline(s.getDeadline());
                stage.setSortOrder(s.getSortOrder()!=null? s.getSortOrder(): null);
                stage.setStartsAt(s.getStartsAt());
                stagesFinal.add(stage);
            }
            goal.setStages(stagesFinal);
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
                    return new UserNotFoundException();
                });
        Goal goal = goalRepository.findByUserAndId(user, goalID).
                orElseThrow(() ->  {
                    log.warn("Delete goal failed: goal not found userId={} goalId={}", userId, goalID);
                    return new GoalNotFoundException(goalID);
                });
        goalRepository.delete(goal);
        log.info("Goal deleted successfully id={} userId={}", goal.getId(), userId);
        return  ("Goal with ID:"+goal.getId()+" was deleted" );
    }

    public Page<Goal>  findAllByParameters(String token, ParametersForSearching parameters) {
        Long userId = jwtService.extractId(token);


        String sortField = parameters.getSort() != null ? parameters.getSort() : "createdAt";

        Sort.Direction direction;

        if ("asc".equalsIgnoreCase(parameters.getOrder())){
            direction = Sort.Direction.ASC;
        }
        else {
            direction = Sort.Direction.DESC;
        }

        List<Goal.GoalStatus> statuses = null;
        if ( parameters.getStatus()!= null){
            statuses = new ArrayList<>();
            for (String s: parameters.getStatus().split(",")){
                try {
                    statuses.add(Goal.GoalStatus.valueOf(s.trim().toUpperCase()));
                }
                catch (Exception e){
                     throw  new IllegalArgumentException("Невалидный статус ");
                }

            }
        }
        List <Goal.PriorityStatus> prioritets = null;
        if (parameters.getPriority()!=null) {
             prioritets = new ArrayList<>();
             for (String s: parameters.getPriority().split(",")){
                 try {
                     prioritets.add(Goal.PriorityStatus.valueOf(s.toUpperCase()));
                 }
                 catch (Exception e){
                    throw  new IllegalArgumentException ("Невалидный приоритет ");
                 }

            }
        }









        Pageable pageable = PageRequest.of(parameters.getPage(), parameters.getSize(), Sort.by(direction,sortField));
        return goalRepository.findByFilters(userId,statuses,prioritets,pageable);

    }

}
