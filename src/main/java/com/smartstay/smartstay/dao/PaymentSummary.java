package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String customerId;
    private String customerStatus;
    private String customerMailId;
    private String customerMobile;
    private String hostelId;
    //when paying to advance or rental
    private Double creditAmount;
    //when generating invoice/advance
    private Double debitAmount;
    private Double balance;
    private String lastInvoice;
    private Double lastPayment;
    private Date lastUpdate;
}
