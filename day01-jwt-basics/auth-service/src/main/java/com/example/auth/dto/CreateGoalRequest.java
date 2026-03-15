package com.example.auth.dto;

import com.example.auth.model.Stage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.List;

/** Тело запроса на создание цели: название, описание и список этапов (можно пустой). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class CreateGoalRequest {

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("start_date")
    private LocalDate startdate;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("daily_time_minutes")
    private Integer daily_time_minutes;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("stages")
    private List<StageCreateRequest> stages;


    public List<Stage> getStagesFromDto(List<StageCreateRequest> stagesDto) {
        List < Stage> stages = stagesDto.stream().map(dto ->{
            Stage stage = new Stage();
            stage.setSortOrder(dto.getSortOrder());
            stage.setStartsAt(dto.getStartsAt());
            stage.setDeadline(dto.getDeadline());
            stage.setEstimatedMinutes(dto.getEstimatedMinutes());
            if (dto.getPriority() != null) {
                stage.setPriority(Stage.PriorityStage.valueOf(dto.getPriority().toUpperCase()));
            }
            stage.setDescription(dto.getDescription());
            stage.setTitle(dto.getTitle());
            return stage;
        }).toList();
        return stages;
    }
    
}
