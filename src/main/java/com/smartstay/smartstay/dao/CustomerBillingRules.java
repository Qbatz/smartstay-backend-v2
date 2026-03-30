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
public class CustomerBillingRules {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String billingRuleId;
    private Integer billingDay;
    private String customerId;
    private String hostelId;
    private Boolean isActive;
    private Date createdAt;
}
