package com.example.auth.service;

import com.example.auth.dto.ResultCreateRequest;
import com.example.auth.dto.UpdatedStage;
import com.example.auth.exception.GoalNotFoundException;
import com.example.auth.exception.StageNotFoundException;
import com.example.auth.exception.UserNotFoundException;
import com.example.auth.model.Goal;
import com.example.auth.model.Result;
import com.example.auth.model.User;
import com.example.auth.repository.GoalRepository;
import com.example.auth.repository.ResultRepository;
import com.example.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.List;

@Service
@Slf4j
public class ResultService {
    private final ResultRepository resultRepository;
    private  final  JwtService jwtService;
    private  final UserRepository userRepository;
    private  final GoalRepository goalRepository;

    public  ResultService(ResultRepository resultRepository, JwtService jwtService,UserRepository userRepository,GoalRepository goalRepository){
        this.resultRepository = resultRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
    }

    @Transactional
    public Result  CreateResult(String token, ResultCreateRequest request, Long goalId){

        //проверка что строка и фотки не пустые
        boolean noText = request.getDescription() == null || request.getDescription().isBlank();
        boolean noImages = request.getImages() == null || request.getImages().isEmpty();
        if (noText && noImages) {
            throw new IllegalArgumentException("Нужно описание или хотя бы одно фото");
        }

        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        Goal goal = goalRepository.findByUserAndId(user, goalId)
                .orElseThrow(() -> new GoalNotFoundException(goalId));


        Result result  = new Result();
        result.setGoal(goal);
        result.setDescription(request.getDescription());
        result.setImages(request.getImages());

        return  resultRepository.save(result);
    }
    @Transactional
    public  Result updateResult(String token, ResultCreateRequest request, Long goalId, Long resultid){
        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        Goal goal = goalRepository.findByUserAndId(user, goalId)
                .orElseThrow(() -> new GoalNotFoundException(goalId));
        Result result = resultRepository.findByIdAndGoal_Id(resultid,goalId)
                .orElseThrow(() -> new IllegalArgumentException("Нет такого результата"));

        if (request.getImages()!=null){
            result.setImages(request.getImages());
        }



        if (request.getDescription()!=null) {
            result.setDescription(request.getDescription());
        }
        return resultRepository.save(result);
    }
    @Transactional
    public void  deleteResult(String token,Long goalId, Long resultId){
        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        Goal goal = goalRepository.findByUserAndId(user, goalId)
                .orElseThrow(() -> new GoalNotFoundException(goalId));

        Result result = resultRepository.findByIdAndGoal_Id(resultId,goalId).orElseThrow(()-> new IllegalArgumentException("Нет такого результата"));
         resultRepository.delete(result);
    }

    public List<Result> allresults(String token, Long goalId){
        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());


        goalRepository.findByUserAndId(user, goalId)
                .orElseThrow(() -> new GoalNotFoundException(goalId));

        List <Result>  results = resultRepository.findAllByGoal_Id(goalId);

        return  results;
    }

}
