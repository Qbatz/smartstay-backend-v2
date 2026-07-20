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
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TenantBanking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bankId;
    private String customerId;
    private String hostelId;
    //available balance.
    private Double amount;
    private Date lastUpdate;
}
