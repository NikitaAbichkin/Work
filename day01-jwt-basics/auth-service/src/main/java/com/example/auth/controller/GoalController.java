package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.model.Goal;
import com.example.auth.model.Stage;
import com.example.auth.service.GoalService;
import com.example.auth.service.StageService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Контроллер целей и этапов.
 * Все эндпоинты требуют заголовок Authorization: Bearer <access_token>.
 * Ответы в формате ApiResponse (success с data или error с текстом).
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final StageService stageService;

    public GoalController(GoalService goalService, StageService stageService) {
        this.goalService = goalService;
        this.stageService = stageService;
    }

    /** Достаём JWT из заголовка Authorization (без слова "Bearer "). */
    private static String tokenFrom(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        return (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
    }

    /** Создать цель. Тело: название, описание, список этапов. Этапы из DTO превращаем в сущности Stage и отдаём в сервис. */

    @PostMapping
    public  ResponseEntity<ApiResponse<Goal>> createGoal(HttpServletRequest httpServletRequest, @RequestBody CreateGoalRequest createGoalRequest){
        String token = tokenFrom(httpServletRequest);
        String description = createGoalRequest.getDescription();
        String title = createGoalRequest.getTitle();
        List<Stage > stages = new ArrayList<>();
        if (createGoalRequest.getStages()!=null){
            for ( StageCreateRequest s : createGoalRequest.getStages()){
                Stage stage = new Stage();
                stage.setTitle(s.getTitle());
                stage.setDescription(s.getDescription());
                stage.setPriority(s.getPriority());
                stage.setEstimatedTime(s.getEstimatedTime());
                stage.setDeadline(s.getDeadline());
                stage.setStartsAt(s.getStartsAt());
                stage.setSortOrder(s.getSortOrder()!= null ? s.getSortOrder(): 0);
                stages.add(stage);
            }
        }
        Goal goal = goalService.createGoal(token,description,title,stages);
        return  ResponseEntity.status(HttpStatus.CREATED)  // 1. создаём builder с кодом 201
                .body(ApiResponse.success(goal));
    }

    /** Список целей пользователя с пагинацией (page, size в query). */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Goal>>> getGoals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest req) {
        String token = tokenFrom(req);
        Page<Goal> goals = goalService.getUserGoals(token, page, size);
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    /** Одна цель по id со всеми этапами (для детального просмотра). */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Goal>>getGoalWithStages(@PathVariable Long id, HttpServletRequest request){
        String token = tokenFrom(request);
        Goal goal = goalService.getGoalWithStages(token, id);
        return  ResponseEntity.ok(ApiResponse.success(goal));

    }

    /** Обновить цель. В теле goalId и поля, которые меняем (title, description, stages). */
    @PutMapping
    public ResponseEntity<ApiResponse<Goal>> updateGoal(@RequestBody UpdatedGoalRequest request, HttpServletRequest req) {
        String token = tokenFrom(req);
        Goal goal = goalService.UpdateGoal(token, request);
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    /** Удалить цель по id. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteGoal(@PathVariable Long id, HttpServletRequest req) {
        String token = tokenFrom(req);
        String message = goalService.DeleteGoal(token, id);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /** Добавить этап к цели. В теле — поля этапа (title, description, deadline и т.д.). */
    @PostMapping("/{goalId}/stages")
    public ResponseEntity<ApiResponse<Stage>> addStage(
            @PathVariable Long goalId,
            @RequestBody StageCreateRequest request,
            HttpServletRequest req) {
        String token = tokenFrom(req);
        Stage stage = stageService.addStage(
                token,
                goalId,
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getEstimatedTime(),
                request.getDeadline(),
                request.getStartsAt(),
                request.getSortOrder() != null ? request.getSortOrder() : 0);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(stage));
    }

    /** Обновить этап. goalId и stageId из пути, остальное в теле (можно частично: status, progress, title и т.д.). */
    @PutMapping("/{goalId}/stages/{stageId}")
    public ResponseEntity<ApiResponse<Stage>> updateStage(
            @PathVariable Long goalId,
            @PathVariable Long stageId,
            @RequestBody UpdatedStage body,
            HttpServletRequest req) {
        String token = tokenFrom(req);
        if (body == null) {
            body = UpdatedStage.builder().build();
        }
        UpdatedStage dto = UpdatedStage.builder()
                .stageId(body.getStageId() != null ? body.getStageId() : stageId)
                .goalId(body.getGoalId() != null ? body.getGoalId() : goalId)
                .title(body.getTitle())
                .description(body.getDescription())
                .priority(body.getPriority())
                .estimatedTime(body.getEstimatedTime())
                .deadline(body.getDeadline())
                .startsAt(body.getStartsAt())
                .progress(body.getProgress())
                .status(body.getStatus())
                .build();
        Stage stage = stageService.updateStage(token, dto);
        return ResponseEntity.ok(ApiResponse.success(stage));
    }

    /** Удалить этап у цели. */
    @DeleteMapping("/{goalId}/stages/{stageId}")
    public ResponseEntity<ApiResponse<String>> deleteStage(
            @PathVariable Long goalId,
            @PathVariable Long stageId,
            HttpServletRequest req) {
        String token = tokenFrom(req);
        String message = stageService.DeleteStage(token, goalId, stageId);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /** Заглушка: разложение цели на этапы через ИИ. Пока просто 202 и "processing". */
    @PostMapping("/{id}/ai-decompose")
    public ResponseEntity<ApiResponse<Map<String, String>>> aiDecompose(@PathVariable Long id, HttpServletRequest req) {
        Map<String, String> stub = Map.of("message", "processing");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(stub));
    }
}
