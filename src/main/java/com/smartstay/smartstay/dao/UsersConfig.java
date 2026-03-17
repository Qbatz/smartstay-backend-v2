package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UsersConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configId;
    private Integer pin;
    private String fcmToken;
    private String fcmWebToken;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users user;
}
