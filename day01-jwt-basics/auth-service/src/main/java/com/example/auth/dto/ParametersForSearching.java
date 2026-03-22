package com.example.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class ParametersForSearching {

    private String status;
    private String priority;
    private  String sort;
    private  String order;
    int page;
    int size;
}
