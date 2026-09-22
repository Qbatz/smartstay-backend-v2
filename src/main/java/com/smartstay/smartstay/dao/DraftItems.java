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
public class DraftItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long draftItemId;
    private String invoiceItem;
    private String otherItem;
    private Double amount;
    private Date fromDate;
    private Date toDate;
    private String sourceId;
    private String sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false)
    private InvoiceDrafts invoiceDrafts;
}
