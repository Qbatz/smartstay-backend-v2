package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.handlers.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "expense_payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpensePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String expenseId;
    private Long expenseItemId;
    private String hostelId;
    private String vendorId;
    private Double paidAmount;
    private String paymentMethod;
    private String bankId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    private String transactionId;
    private String notes;
    private String imageUrl;

    // S3 URLs of all receipt/bill images uploaded with this payment.
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> imageUrls;

    // Audit fields, populated by the service on create/update.
    private String createdBy;
    private String updatedBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;
}
