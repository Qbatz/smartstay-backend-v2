package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private InvoicesV1 invoice;
}
