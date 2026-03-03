package com.example.auth.dto;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.concurrent.ThreadPoolExecutor;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("success");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(String error){
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("error");
        response.setError(error);
        return response;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}


