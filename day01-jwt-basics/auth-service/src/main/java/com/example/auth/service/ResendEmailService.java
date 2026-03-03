package com.example.auth.service;


import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.apikeys.model.ApiKey;
import com.resend.services.emails.model.CreateEmailOptions;
import org.hibernate.jpa.internal.ExceptionMapperLegacyJpaImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ResendEmailService {
    @Value("${resend.api.key}")
    private String apiKey;


    public void sendCode(String email, String code){
        Resend resend = new Resend(apiKey);


        CreateEmailOptions params = new CreateEmailOptions.Builder()
                .from("swag@wordtool.ru")
                .to(email)
                .subject("Your code to verify")
                .html("<h1>Привет!</h1><p>Твой код: <strong>" + code + "</strong></p>")
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new RuntimeException("возникла ошибка с resend:  "+ e);
        }

    }



}
