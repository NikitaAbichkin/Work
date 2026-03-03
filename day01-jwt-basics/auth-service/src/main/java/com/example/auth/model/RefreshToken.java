package com.example.auth.model;

import jakarta.persistence.*;


import java.time.LocalDateTime;


@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)

    private Long id;

    @Column (name = "token", nullable = false)
    private  String token;

    @Column (name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;






    public String getToken(){
        return token;
    }
    public void setToken(String token){
        this.token = token;
    }
    public LocalDateTime getExpiresAt(){
        return expiresAt;
    }
    public void setExpiresAt(LocalDateTime expiresAt){
        this.expiresAt = expiresAt;
    }

    public User getUser(){
        return user;
    }
    public  void setUser( User user_id){
        this.user = user_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}