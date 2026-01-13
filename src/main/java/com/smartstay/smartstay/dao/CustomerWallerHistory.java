package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class CustomerWallerHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    private Double transactionDate;
    private Double amount;
    private String billingStatus;
    private String customerId;
    private String sourceId;
    //Rent or amenity or EB or any other source
    private String sourceType;
    private Date createdAt;
    private String createdBy;
}
