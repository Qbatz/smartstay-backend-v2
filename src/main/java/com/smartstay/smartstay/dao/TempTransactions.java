package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *
 * backup for bank transactions v1
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempTransactions {
    @Id
    private String transactionId;
    //Transaction type enum
    private String type;
    //    private String transactionType;
    private Double paidAmount;
    private String createdBy;
    private Date createdAt;
    //From payment status ENUM
    private String status;
    private String invoiceId;
    private String hostelId;
    private String isInvoice;
    private String customerId;
    private Date paymentDate;
    //From receipt mode Enum
    private String transactionMode;
    private String source;
    //auto generated
    private String transactionReferenceId;
    private String receiptUrl;
    //card/gpay or cash or bank
    private String bankId;
    //entered by customer
    private String referenceNumber;
    private Date paidAt;
    private String updatedBy;
}
