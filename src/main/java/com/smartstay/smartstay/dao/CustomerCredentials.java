package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String xuid;
    private String customerMobile;
    private String customerPin;
    private boolean isPinVerified;
    private String defaultHostel;
    private Date createdAt;
}
