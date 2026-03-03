package com.example.auth.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "confirmation_codes")
public class ConfirmationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(nullable = false,length = 4)
    private  String code;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;










    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }


    public void setUser(User user) {
        this.user = user;
    }

    public User getUser(){
        return  user;
    }


}










