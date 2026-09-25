package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.converters.CancelledInvoiceConverter;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.invoices.CancelledInvoice;
import com.smartstay.smartstay.handlers.DeductionsConverter;
import com.smartstay.smartstay.handlers.StringListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvoiceDrafts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long draftId;
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
    boolean isEdited;
    Double discountAmount;
    String createdBy;
    String updatedBy;
    Date invoiceGeneratedDate;
    Date cancelledDate;
    Date invoiceDueDate;
    Date invoiceDate;
    Date invoiceStartDate;
    Date invoiceEndDate;
    Date createdAt;
    Date updatedAt;

    @OneToMany(mappedBy = "invoiceDrafts", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DraftItems> listItems = new ArrayList<>();
}
