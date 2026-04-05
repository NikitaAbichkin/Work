package com.example.auth.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordResponce {
    private String email;
    private  String code;
    private  String newPassword;
}
