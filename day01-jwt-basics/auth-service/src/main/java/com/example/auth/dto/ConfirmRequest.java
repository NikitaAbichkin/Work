package com.example.auth.dto;

public class ConfirmRequest {
    private String username;
    private String code ;

    public void setUsername(String username ){
        this.username = username;
    }

    public  void  setCode (String code){
        this.code = code;

    }
    public String getUsername(){
        return username;
    }
    public  String getCode(){
        return  code;
    }
}

