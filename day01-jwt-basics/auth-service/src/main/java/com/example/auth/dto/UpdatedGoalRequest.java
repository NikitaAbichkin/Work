package com.example.auth.dto;

import com.example.auth.model.Stage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Jacksonized // Помогает Jackson корректно десериализовать Builder из JSON
public class UpdatedGoalRequest {
    private  Long goalId ;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("start_date")
    private LocalDate startdate;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("daily_time_minutes")
    private Integer daily_time_minutes;

    private String description;

    private String title;

    private List<Stage> stages;

}




