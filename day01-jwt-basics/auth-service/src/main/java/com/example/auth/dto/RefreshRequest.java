package com.example.auth.dto;

public class RefreshRequest {
    private String RefreshToken;

    public String getRefreshToken(){
        return  RefreshToken;
    }
    public  void setRefreshToken( String RefreshToken){
        this.RefreshToken = RefreshToken;
    }
}
