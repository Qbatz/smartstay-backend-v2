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
    private String userId;
    private Date createdAt;
    private boolean isVerified;
}
