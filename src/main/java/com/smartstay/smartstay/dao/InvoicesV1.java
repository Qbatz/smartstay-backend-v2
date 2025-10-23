package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

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
    //Advance or monthly rent or Booking amount or settlement
    String invoiceType;
    Double basePrice;
    //this includes GST
    Double totalAmount;
    Double paidAmount;
    Double gst;
    Double cgst;
    Double sgst;
    Double gstPercentile;
    String paymentStatus;
    //will be applicable only for additional amount deduction when invoice type is others
    String othersDescription;
    //Mode will be manual and automatic
    String invoiceMode;
    boolean isCancelled;
    List<String> cancelledInvoices;
    String createdBy;
    String updatedBy;
    Date invoiceGeneratedDate;
    Date invoiceDueDate;
    Date invoiceStartDate;
    Date invoiceEndDate;
    Date createdAt;
    Date updatedAt;

    @OneToMany(mappedBy = "invoice", orphanRemoval = true, cascade = CascadeType.ALL)
    List<InvoiceItems> invoiceItems;
}
