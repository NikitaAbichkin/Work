package com.example.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

/** Данные одного этапа при создании цели или при добавлении этапа к цели (название, дедлайн, порядок и т.д.). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class StageCreateRequest {
    private String title;
    private String description;
    private String priority;
    private Integer estimatedMinutes;
    private LocalDate deadline;
    private LocalDate startsAt;
    private Integer sortOrder;
    private String status;
}