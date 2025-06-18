package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Entity
@Getter
@Setter
public class UserOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Integer otp;
    private Date createdAt;
    private Date otpValidity;
    private boolean isVerified;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;
}
