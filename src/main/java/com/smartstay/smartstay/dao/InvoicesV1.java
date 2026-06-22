package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.handlers.DeductionsConverter;
import com.smartstay.smartstay.handlers.StringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
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
    //this is for redemption.
    Double balanceAmount;
    Double subTotal;
    Double gst;
    Double cgst;
    Double sgst;
    Double gstPercentile;
    String paymentStatus;
    Double deductionAmount;
    //will be applicable only for additional amount deduction when invoice type is others
    String othersDescription;
    //Mode will be manual and automatic
    String invoiceMode;
    boolean isCancelled;
    boolean isDiscounted;
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    List<String> cancelledInvoices;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = DeductionsConverter.class)
    List<Deductions> deductions;
    String invoiceUrl;
    String createdBy;
    String updatedBy;
    Date invoiceGeneratedDate;
    Date cancelledDate;
    Date invoiceDueDate;
    Date invoiceStartDate;
    Date invoiceEndDate;
    Date createdAt;
    Date updatedAt;

    @OneToMany(mappedBy = "invoice", orphanRemoval = true, cascade = CascadeType.ALL)
    List<InvoiceItems> invoiceItems;
}
