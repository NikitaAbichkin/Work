package com.example.auth.dto;

import java.time.LocalDateTime;

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
    private String estimatedTime;
    private LocalDateTime deadline;
    private LocalDateTime startsAt;
    private Integer progress;
    /** Статус этапа: IN_PROGRESS, FROZEN, COMPLETED */
    private String status;
}
