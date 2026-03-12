package com.example.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/** Тело запроса на создание цели: название, описание и список этапов (можно пустой). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class CreateGoalRequest {
    private String title;
    private String description;
    private List<StageCreateRequest> stages;
}
