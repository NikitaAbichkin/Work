package com.example.auth.dto;

import ch.qos.logback.core.joran.action.PreconditionValidator;
import com.example.auth.model.Stage;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Jacksonized // Помогает Jackson корректно десериализовать Builder из JSON
public class UpdatedGoalRequest {
    private  Long goalId ;

    private String description = null;


    private String title = null;


    private List<Stage> stages = null;

}




