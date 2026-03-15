package com.example.auth.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdatedStage {
    private Long stageId;
    private Long goalId;
    private String title;
    private String priority;
    private String description;
    private Integer estimatedMinutes;
    private LocalDate deadline;
    private LocalDate startsAt;
    private Integer progress;
    /** Статус этапа: IN_PROGRESS, FROZEN, COMPLETED */
    private String status;
}