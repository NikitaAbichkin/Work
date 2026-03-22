package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.model.Goal;
import com.example.auth.model.Result;
import com.example.auth.model.Stage;
import com.example.auth.repository.StageRepository;
import com.example.auth.service.AiPlanService;
import com.example.auth.service.GoalService;
import com.example.auth.service.ResultService;
import com.example.auth.service.StageService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Контроллер целей и этапов.
 * Все эндпоинты требуют заголовок Authorization: Bearer <access_token>.
 * Ответы в формате ApiResponse (success с data или error с текстом).
 */
@RestController
@RequestMapping("/api/v1/goals")
@Slf4j
public class GoalController {
    private  final StageRepository stageRepository;
    private final GoalService goalService;
    private final StageService stageService;
    private final ResultService resultService;
    private  final AiPlanService aiPlanService;

    public GoalController(GoalService goalService, StageService stageService, StageRepository stageRepository, ResultService resultService,AiPlanService aiPlanService) {
        this.goalService = goalService;
        this.stageService = stageService;
        this.stageRepository = stageRepository;
        this.resultService = resultService;
        this.aiPlanService = aiPlanService;
    }

    /**
     * Достаём JWT из заголовка Authorization (без слова "Bearer ").
     */
    private static String tokenFrom(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        return (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
    }

    /**
     * Создать цель. Тело: название, описание, список этапов.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Goal>> createGoal(HttpServletRequest httpServletRequest,
                                                        @RequestBody CreateGoalRequest createGoalRequest) {
        String token = tokenFrom(httpServletRequest);
        log.info("HTTP POST /api/v1/goals createGoal title={}", createGoalRequest.getTitle());
        Goal goal = goalService.createGoal(token, createGoalRequest);
        return ResponseEntity.status(HttpStatus.CREATED)  // 1. создаём builder с кодом 201
                .body(ApiResponse.success(goal));
    }

    /**
     * Список целей пользователя с пагинацией (page, size в query).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Goal>>> getGoals(
            HttpServletRequest request, @ModelAttribute ParametersForSearching parameters ) {
        String token = tokenFrom(request);
        Page<Goal> pages = goalService.findAllByParameters(token, parameters);
        return ResponseEntity.ok(ApiResponse.success(pages));

    }

    /**
     * Одна цель по id со всеми этапами (для детального просмотра).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Goal>> getGoalWithStages(@PathVariable Long id, HttpServletRequest request) {
        String token = tokenFrom(request);
        log.info("HTTP GET /api/v1/goals/{} getGoalWithStages", id);
        Goal goal = goalService.getGoalWithStages(token, id);
        return ResponseEntity.ok(ApiResponse.success(goal));

    }

    /**
     * Частично обновить цель по id. В теле — только изменяемые поля.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Goal>> updateGoal(@PathVariable Long id,
                                                        @RequestBody UpdatedGoalRequest request,
                                                        HttpServletRequest req) {
        String token = tokenFrom(req);
        log.info("HTTP PATCH /api/v1/goals/{} updateGoal", id);
        if (request.getGoalId() == null) {
            request.setGoalId(id);
        } else if (!request.getGoalId().equals(id)) {
            throw new IllegalArgumentException("goalId в теле не совпадает с id в пути");
        }
        Goal goal = goalService.UpdateGoal(token, request);
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    /**
     * Удалить цель по id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id, HttpServletRequest req) {
        String token = tokenFrom(req);
        log.info("HTTP DELETE /api/v1/goals/{} deleteGoal", id);
        goalService.DeleteGoal(token, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Добавить задачу к цели. В теле — поля задачи (title, description, deadline и т.д.).
     */
    @PostMapping("/{goalId}/tasks")
    public ResponseEntity<ApiResponse<Stage>> addStage(
            @PathVariable Long goalId,
            @RequestBody StageCreateRequest request,
            HttpServletRequest req) {
        String token = tokenFrom(req);
        log.info("HTTP POST /api/v1/goals/{}/tasks addStage", goalId);
        Stage stage = stageService.addStage(
                token,
                goalId,
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getEstimatedMinutes(),
                request.getDeadline(),
                request.getStartsAt(),
                request.getSortOrder() != null ? request.getSortOrder() : 0);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(stage));
    }

    /**
     * Обновить задачу. goalId и stageId из пути, остальное в теле (можно частично: status, progress, title и т.д.).
     */
    @PatchMapping("/{goalId}/tasks/{stageId}")
    public ResponseEntity<ApiResponse<Stage>> updateStage(
            @PathVariable Long goalId,
            @PathVariable Long stageId,
            @RequestBody UpdatedStage body,
            HttpServletRequest req) {
        String token = tokenFrom(req);
        log.info("HTTP PUT /api/v1/goals/{}/tasks/{} updateStage", goalId, stageId);
        if (body == null) {
            body = UpdatedStage.builder().build();
        }
        UpdatedStage dto = UpdatedStage.builder()
                .stageId(body.getStageId() != null ? body.getStageId() : stageId)
                .goalId(body.getGoalId() != null ? body.getGoalId() : goalId)
                .title(body.getTitle())
                .description(body.getDescription())
                .priority(body.getPriority())
                .estimatedMinutes(body.getEstimatedMinutes())
                .deadline(body.getDeadline())
                .startsAt(body.getStartsAt())
                .progress(body.getProgress())
                .status(body.getStatus())
                .build();
        Stage stage = stageService.updateStage(token, dto);
        return ResponseEntity.ok(ApiResponse.success(stage));
    }

    /**
     * Удалить этап у цели.
     */
    @DeleteMapping("/{goalId}/tasks/{stageId}")
    public ResponseEntity<ApiResponse<String>> deleteStage(
            @PathVariable Long goalId,
            @PathVariable Long stageId,
            HttpServletRequest req) {
        String token = tokenFrom(req);
        log.info("HTTP DELETE /api/v1/goals/{}/tasks/{} deleteStage", goalId, stageId);
        String message = stageService.deleteStage(token, goalId, stageId);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * Заглушка: разложение цели на этапы через ИИ. Пока просто 202 и "processing".
     */
    @PostMapping("/ai-decompose")
    public ResponseEntity<ApiResponse<AIPlanResponce>> aiDecompose(HttpServletRequest httpServletRequest, @RequestBody AiPlanRequest request) {
        String token = tokenFrom(httpServletRequest);
        log.info("HTTP POST /api/v1/goals/ai-decompose");
        AIPlanResponce aiPlanResponce = aiPlanService.generatePlan(token, request.getPrompt());
        return  ResponseEntity.ok(ApiResponse.success(aiPlanResponce));

    }
    @PostMapping("/ai-help")
    public ResponseEntity<ApiResponse<AIPlanResponce>> aiHelp(HttpServletRequest httpServletRequest, @RequestBody AIHelpGoalReqest request) {
        String token = tokenFrom(httpServletRequest);
        AIPlanResponce aiPlanResponce = aiPlanService.HelpWithGoal(token,request.getPromt(), request.getGoalId());
        return ResponseEntity.ok(ApiResponse.success(aiPlanResponce));
    }


    @GetMapping("/goals")
    public ResponseEntity<ApiResponse<Page<Goal>>> searchForParameters(@ModelAttribute ParametersForSearching parameters, HttpServletRequest request) {
        String token = tokenFrom(request);
        Page<Goal> pages = goalService.findAllByParameters(token, parameters);
        return ResponseEntity.ok(ApiResponse.success(pages));
    }

    @GetMapping("/{goalId}/stages")
    public ResponseEntity<ApiResponse<Page<Stage>>> allstages(HttpServletRequest request,  @ModelAttribute  Allstages allstages ){
        String token = tokenFrom(request);
        Page<Stage> stages = stageService.getStagesByGoal(token,allstages);
        return ResponseEntity.ok(ApiResponse.success(stages));
    }


    @GetMapping("{goalId}/tasks/{stageId}")
    public ResponseEntity<ApiResponse<Stage>> onlyOneStage(HttpServletRequest request, @PathVariable Long goalId, @PathVariable Long stageId){
        String token =  tokenFrom(request);
        Stage stage = stageService.oneStage(token,stageId);
        return ResponseEntity.ok(ApiResponse.success(stage));
    }

    // ==================== Results ====================

    @GetMapping("/{goalId}/results")
    public ResponseEntity<ApiResponse<List<Result>>> getResults(@PathVariable Long goalId, HttpServletRequest req) {
        String token = tokenFrom(req);
        log.info("HTTP GET /api/v1/goals/{}/results", goalId);
        List<Result> results = resultService.allresults(token, goalId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping("/{goalId}/results")
    public ResponseEntity<ApiResponse<Result>> createResult(@PathVariable Long goalId,
                                                             @RequestBody ResultCreateRequest request,
                                                             HttpServletRequest req) {
        String token = tokenFrom(req);
        log.info("HTTP POST /api/v1/goals/{}/results", goalId);
        Result result = resultService.CreateResult(token, request, goalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @PatchMapping("/{goalId}/results/{resultId}")
    public ResponseEntity<ApiResponse<Result>> updateResult(@PathVariable Long goalId,
                                                             @PathVariable Long resultId,
                                                             @RequestBody ResultCreateRequest request,
                                                             HttpServletRequest req) {
        String token = tokenFrom(req);
        log.info("HTTP PATCH /api/v1/goals/{}/results/{}", goalId, resultId);
        Result result = resultService.updateResult(token, request, goalId, resultId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{goalId}/results/{resultId}")
    public ResponseEntity<Void> deleteResult(@PathVariable Long goalId,
                                              @PathVariable Long resultId,
                                              HttpServletRequest req) {
        String token = tokenFrom(req);
        log.info("HTTP DELETE /api/v1/goals/{}/results/{}", goalId, resultId);
        resultService.deleteResult(token, goalId, resultId);
        return ResponseEntity.noContent().build();
    }

}
