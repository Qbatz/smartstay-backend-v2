package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class InvoiceItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceItemId;
    private String invoiceItem;
    private String otherItem;
    private Double amount;
    private Date fromDate;
    private Date toDate;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private InvoicesV1 invoice;
}
