package com.example.auth.exception;

public class InvalidCredentialsException  extends  RuntimeException{
    public InvalidCredentialsException (){
        super("Неверные учётные данные");
    }
}
