package com.example.auth.service;


import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;


@Service
@Slf4j
public class ResendEmailService {
    @Value("${resend.api.key}")
    private String apiKey;


    public String sendCode(String email, String code){

        log.info("Sending confirmation code email to {}", email);
        Resend resend = new Resend(apiKey);


        CreateEmailOptions params = new CreateEmailOptions.Builder()
                .from("swag@wordtool.ru")
                .to(email)
                .subject("Your code to verify")
                .html("<h1>Привет!</h1><p>Твой код: <strong>" + code + "</strong></p>")
                .build();

        try {
            resend.emails().send(params);
            log.info("Confirmation email sent successfully to {}", email);
        } catch (ResendException e) {
            log.error("Error while sending confirmation email to {}", email, e);
            throw new RuntimeException("возникла ошибка с resend:  "+ e);
        }
        return  code ;

    }



}
