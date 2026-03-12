package com.example.auth.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String status;
    private T data;
    private HashMap<String, String> error;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("success");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(String ErrorType, String message){
        ApiResponse<T> response = new ApiResponse<>();
        HashMap<String, String > result = new HashMap<>();
        response.setStatus("error");
        result.put("code",ErrorType);
        result.put("message",message);
        response.setError(result);
        return response;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
        public HashMap<String, String> getError() { return error; }  
        public void setError(HashMap<String, String> error) { this.error = error; }
}




