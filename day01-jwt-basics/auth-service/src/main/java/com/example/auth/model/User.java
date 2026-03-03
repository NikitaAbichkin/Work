package com.example.auth.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private  String  username;


    @Column(name = "hashed_password",nullable = false)
    private String hashedPassword;

    @Column( unique = true, nullable = false)
    private  String email;

    @Column(nullable = false)
    private String status = "NOT_CONFIRMED";










    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public  String getUsername(){
        return  username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {this.hashedPassword = hashedPassword;}

    public String getStatus() {return  status;}
    public  void setStatus(String status){this.status = status;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}

}