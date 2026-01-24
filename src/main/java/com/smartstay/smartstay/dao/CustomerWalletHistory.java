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
public class CustomerWalletHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    private Date transactionDate;
    private Double amount;
    private String billingStatus;
    private String customerId;
    //Amenity id for amenities and eb reading id for eb readings
    private String sourceId;
    //Rent or amenity or EB or any other source
    private String sourceType;
    //from wallet transaction type enum
    //debit -> owner has to return
    //credit -> owner should receive
    private String transactionType;
    private Date billStartDate;
    private Date billEndDate;
    private Date createdAt;
    private String createdBy;


}
