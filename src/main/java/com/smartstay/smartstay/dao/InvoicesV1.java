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
public class InvoicesV1 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String invoiceId;
    String customerId;
    String hostelId;
    String invoiceNumber;
    String customerMobile;
    String customerMailId;
    //Advance or monthly rent or Booking amount
    String invoiceType;
    Double amount;
    Double gst;
    Double cgst;
    Double sgst;
    Integer gstPercentile;
    String paymentStatus;
    //will be applicable only for additional amount deduction when invoice type is others
    String othersDescription;
    //Mode will be manual and automatic
    String invoiceMode;
    String createdBy;
    String updatedBy;
    Date invoiceGeneratedDate;
    Date invoiceDueDate;
    Date createdAt;
    Date updatedAt;
}
