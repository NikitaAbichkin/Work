package com.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AIPlanResponce {

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

}
