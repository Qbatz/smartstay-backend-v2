package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionV1 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String transactionId;
    private String type;
//    private String transactionType;
    private Double paidAmount;
    private String createdBy;
    private Date createdAt;
    private String status;
    private String invoiceId;
    private String hostelId;
    private String isInvoice;
    private String customerId;
    private Date paymentDate;
    //auto generated
    private String transactionReferenceId;
    //card/gpay or cash or bank
    private String bankId;
    //entered by customer
    private String referenceNumber;
    private Date paidAt;
    private String updatedBy;
}
