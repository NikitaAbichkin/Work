package com.example.auth.service;

import com.example.auth.dto.UpdatedGoalRequest;
import com.example.auth.model.Goal;
import com.example.auth.model.Stage;
import com.example.auth.model.User;
import com.example.auth.repository.GoalRepository;
import com.example.auth.repository.StageRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
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

        // создаем настройку то как нам выдавать
        Pageable pageable = PageRequest.of(page,size,Sort.by("id"));

        return  goalRepository.findByUserId(userId, pageable);

         }

    @Transactional(readOnly = true)
    public Goal getGoalWithStages(String token, Long goalid){
        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Goal goal  = goalRepository.findByUserAndId(user,goalid).orElseThrow(()-> new IllegalArgumentException("Нет такой цели"));
       
        goal.getStages().size(); // чтобы подтянуть все задачи цели
        return goal;
    }

    @Transactional
    public Goal createGoal(String token, String description, String title, List<Stage> stages ){

        Long userId = jwtService.extractId(token);
        if (goalRepository.findByUserIdAndTitle(userId, title).isPresent()){
            throw new IllegalArgumentException("Цель с названием '" + title + "' уже существует");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Goal goal = new Goal();
        goal.setUser(user);
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setStages(stages);
        
        return goalRepository.save(goal);
    }

    @Transactional
    public Goal UpdateGoal(String token , UpdatedGoalRequest request){
        Long userId = jwtService.extractId(token);

        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("Нет такого юзера"));
        Goal goal = goalRepository.findByUserAndId(user, request.getGoalId())
                .orElseThrow(()-> new IllegalArgumentException("Нет такой задачи"));
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

       return  goalRepository.save(goal);
    }
    @Transactional
    public String  DeleteGoal(String token , Long goalID){

       Long userId =  jwtService.extractId(token);
        User user =  userRepository.findById(userId )
                .orElseThrow(()-> new IllegalArgumentException("такого юзера нет"));
        Goal goal = goalRepository.findByUserAndId(user, goalID).
                orElseThrow(()->  new IllegalArgumentException(" Нет такой задачи "));
        goalRepository.delete(goal);
        return  ("Goal with ID:"+goal.getId()+" was deleted" );
    }



}
