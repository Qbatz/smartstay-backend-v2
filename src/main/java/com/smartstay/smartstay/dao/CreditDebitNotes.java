package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CreditDebitNotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    //credit or debit
    private String type;
    private Double amount;
    private String source;
    private String invoiceId;
    private String bookingId;
    private String reason;
    private String customerId;
    private String assetId;
    private String referenceNumber;
    private String debitedFrom;
    private String creditedTo;
    private String createdBy;
    private Date createdAt;
}
